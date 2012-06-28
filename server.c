/* A simple server in the internet domain using TCP
   The port number is passed as an argument */
#include <stdio.h>
#include <stdlib.h>
#include <time.h>
#include <string.h>
#include <unistd.h>
#include <sys/types.h> 
#include <sys/socket.h>
#include <netinet/in.h>

#include <mysql.h>

#define FIELD_EMPTY		0
#define FIELD_TINYINT	1
#define FIELD_SMALLINT	2
#define FIELD_INT		3
#define FIELD_BIGINT	4
#define FIELD_FLOAT		10
#define FIELD_DOUBLE	11
#define FIELD_TEXT		20

#define MAX_SETCOUNT		3
#define MAX_FIELDCOUNT		8
#define MAX_VALUELENGTH		1024

const char *set_definitions[][MAX_FIELDCOUNT] = {
	{"state", "TINYINT", "TINYINT", "TINYINT", "TINYINT", "TINYINT"},
	{"gps", "BIGINT", "DOUBLE", "DOUBLE", "DOUBLE", "DOUBLE", "DOUBLE", "DOUBLE"},
	{"click", "INT", "INT"}
};

struct Field{
	char *name;
	int type;
};

struct Set{
	char *name;
	char *table;
	struct Field fields[MAX_FIELDCOUNT];
	int field_cnt;
	char *values[MAX_FIELDCOUNT];
};

char *device_table = "devices";
char *event_table = "events";

struct Set sets[] = {
	{.name = "state", .table = NULL, .fields = { 
		{ .name = "screen",		.type = FIELD_TINYINT}, 
		{ .name = "bluetooth",	.type = FIELD_TINYINT}, 
		{ .name = "gps",		.type = FIELD_TINYINT}, 
		{ .name = "locked",		.type = FIELD_TINYINT}, 
		{ .name = "alert",		.type = FIELD_TINYINT},
	}, .field_cnt = 5},
	{.name = "gps", .table = "locations", .fields = { 
		{ .name = "time",	.type = FIELD_BIGINT},
		{ .name = "lat",	.type = FIELD_DOUBLE}, 
		{ .name = "lng",	.type = FIELD_DOUBLE}, 
		{ .name = "alt",	.type = FIELD_DOUBLE}, 
		{ .name = "spd",	.type = FIELD_DOUBLE}, 
		{ .name = "acc",	.type = FIELD_DOUBLE}, 
		{ .name = "bear",	.type = FIELD_DOUBLE}, 
	}, .field_cnt = 7},
	{.name = "click", .table = "click", .fields = { 
		{ .name = "x",		.type = FIELD_INT},
		{ .name = "y",	 	.type = FIELD_INT},
	}, .field_cnt = 2},
		
};

int set_count = MAX_SETCOUNT;

struct Header{
	uint16_t		magic;
	uint16_t		version;
	uint32_t    event_count;
	uint32_t    payload_size;
	uint8_t			uuid[68];
};

struct Index{
	uint64_t time_stamp;
	uint32_t data_size;
	uint32_t type;
};

struct Response{
	uint64_t code;
	uint64_t last_log;
};

int bitfield_size = 8;

MYSQL *mysql_conn;
MYSQL_RES *mysql_res;
MYSQL_ROW mysql_row;

char *mysql_server = "localhost";
char *mysql_user = "root";
char *mysql_password = NULL; /* set me first */
char *mysql_database = "Textbuster";

void error(const char *msg)
{
    perror(msg);
    exit(1);
}

int kbhit()
{
    struct timeval tv = { 0L, 0L };
    fd_set fds;
    FD_ZERO(&fds);
    FD_SET(0, &fds);
    return select(1, &fds, NULL, NULL, &tv);
}

int main(int argc, char *argv[]){

	printf("MYSQL\n=====\n Server   : %s\n Database : %s\n User     : %s\n\n", mysql_server, mysql_database, mysql_user);
	
	printf("STRUCTS\n=======\n");
	printf("Header   : %i\n", sizeof(struct Header) );
	printf("Index    : %i\n", sizeof(struct Index) );
	printf("Response : %i\n", sizeof(struct Response) );
	printf("\n");
	
	printf("DATATYPES\n=========\n");
	printf(" TINYINT  : %i\n", sizeof(char));
	printf(" SMALLINT : %i\n", sizeof(uint16_t));
	printf(" INT      : %i\n", sizeof(uint32_t));
	printf(" BIGINT   : %i\n", sizeof(uint64_t));
	printf(" FLOAT    : %i\n", sizeof(float));
	printf(" DOUBLE   : %i\n", sizeof(double));
	printf("\n");
	
	printf("LOGSET_DEFINITIONS = {\n");
	int s; for(s = 0; s < set_count; s++){
	 printf("\t{\"%s\", ", sets[s].name);
	 int f; for(f = 0; f < sets[s].field_cnt; f++){
		printf("\"");
		switch(sets[s].fields[f].type){
			case FIELD_TINYINT: printf("TINYINT"); break;
			case FIELD_SMALLINT: printf("SMALLINT"); break;
			case FIELD_INT: printf("INT"); break;
			case FIELD_BIGINT: printf("BIGINT"); break;
			case FIELD_FLOAT: printf("FLAOT"); break;
			case FIELD_DOUBLE: printf("DOUBLE"); break;
			case FIELD_TEXT: printf("TEXT"); break;
		}
		printf("\"");
		if(f + 1 != sets[s].field_cnt) printf(", ");
	 }
	 printf("}");
	 if(s + 1 != set_count) printf(",");
	 printf("\n");
	}
	printf("};\n");
	
	// Open local service socket.
	
	int sockfd, newsockfd, portno = 1234;
	socklen_t clilen;
	
	struct sockaddr_in serv_addr, cli_addr;
	
	int n;
	
	sockfd = socket(AF_INET, SOCK_STREAM, 0);
	
	int optval = 1;
	setsockopt(sockfd, SOL_SOCKET, SO_REUSEADDR, &optval, sizeof optval);
	
	if (sockfd < 0) error("ERROR opening socket");
	
	bzero((char *) &serv_addr, sizeof(serv_addr));
	
	
	serv_addr.sin_family = AF_INET;
	serv_addr.sin_addr.s_addr = INADDR_ANY;
	serv_addr.sin_port = htons(portno);
	
	if (bind(sockfd, (struct sockaddr *) &serv_addr,
		  sizeof(serv_addr)) < 0) 
		  error("ERROR on binding");
	listen(sockfd, 5);
	clilen = sizeof(cli_addr);
	
	while(1){
		 newsockfd = accept(sockfd, (struct sockaddr *) &cli_addr, &clilen);
		 
		 pid_t pID = fork();
		 if (pID == 0){
			
			char* header_buf = malloc(sizeof(struct Header));
			if(header_buf == NULL){ error("ERROR allocating header buffer"); }
			
			if (newsockfd < 0){ error("ERROR on accept"); };
			  
			n = recv(newsockfd, header_buf, sizeof(struct Header), MSG_WAITALL);
			
			if (n != sizeof(struct Header)) { error("ERROR reading header");};
			
			// Read Header.
			
			struct Header *header = (struct Header*)header_buf;
			header->uuid[65] = 0;
		   
			//printf("Header - magic(0x%X), version(%i), events(%i), payload(%i bytes), imei(%s)\n", header->magic, header->version, header->event_count, header->payload_size, header->uuid);
			
			// Read indexes.
			
			char* index_buf = malloc(sizeof(struct Index) * header->event_count);
			if(index_buf == NULL){ error("ERROR allocating index buffer"); }
			
			char* data_buf = malloc(1024 * 64);
			if(data_buf == NULL){ error("ERROR allocating data buffer"); }
			
			n = recv(newsockfd, index_buf, sizeof(struct Index) * header->event_count, MSG_WAITALL);
			
			if (n != sizeof(struct Index) * header->event_count) { error("ERROR reading index");};
			
			int i; for(i = 0; i < header->event_count; i++){
				 
				struct Index *index = (struct Index*)(index_buf + (i * sizeof(struct Index)));
				 
				//printf(" Event - time(%llu) data(%i bytes)\n", index->time_stamp, index->data_size);
				
				int n = recv(newsockfd, data_buf, index->data_size, MSG_WAITALL);
			
				if (n != index->data_size) { error("ERROR reading data");};
				
				char *values[set_count][MAX_FIELDCOUNT]; 
				
				// Loop through sets to load data payload from socket.
				
				int a = set_count / 8 + 1;
				int s; for(s = 0; s < set_count; s++){
					
					char bitfield = data_buf[s / 8];
					if(bitfield & 1 << (s % 8)){
						
						// Loop through fields.
						
						int f; for(f = 0; f < sets[s].field_cnt; f++){
							
							char *value = malloc(MAX_VALUELENGTH);
							if (value == NULL) { error("ERROR allocating value buffer");};
							value[0] = 0;
							
							if(sets[s].fields[f].type == FIELD_TINYINT){
								snprintf(value, MAX_VALUELENGTH, "%i", *(uint8_t*)(data_buf + a));
								a += 1;
							}else if(sets[s].fields[f].type == FIELD_SMALLINT){
								snprintf(value, MAX_VALUELENGTH, "%i", *(uint16_t*)(data_buf + a));
								a += 2;
							}else if(sets[s].fields[f].type == FIELD_INT){
								snprintf(value, MAX_VALUELENGTH, "%i", *(uint32_t*)(data_buf + a));
								a += 4;
							}else if(sets[s].fields[f].type == FIELD_BIGINT){
								snprintf(value, MAX_VALUELENGTH, "%llu", *(uint64_t*)(data_buf + a));
								a += 8;
							}else if(sets[s].fields[f].type == FIELD_FLOAT){
								snprintf(value, MAX_VALUELENGTH, "%f", *(float*)(data_buf + a));
								a += 4;
							}else if(sets[s].fields[f].type == FIELD_DOUBLE){
								snprintf(value, MAX_VALUELENGTH, "%F", *(double*)(data_buf + a));
								a += 8;
							}
							
							void *ptr = realloc(value, strnlen(value, MAX_VALUELENGTH));
							if (ptr == NULL) { error("ERROR reallocating value buffer");};
							
							values[s][f] = value;
							
						}
						
					}else{
						
						values[s][0] = NULL;
						
					}
					
				}
				
				// Loop through sets to process values.
				
				char *queries[2 + set_count];
				
				
				queries[0] = malloc(1024);
				if (queries[0] == NULL) { error("ERROR allocating 1st query buffer");};
				 snprintf(queries[0], 1024,
					"INSERT INTO `%s` (`imei`, `created`, `seen`) VALUES ('%s', NOW(), NOW()) ON DUPLICATE KEY UPDATE `id` = LAST_INSERT_ID(`id`), `seen` = NOW();",
					device_table, header->uuid
				);
				
				queries[1] = malloc(1024);
				if (queries[1] == NULL) { error("ERROR allocating 2nd query buffer");};
				snprintf(queries[1], 1024, "SET @%s_id = LAST_INSERT_ID();", device_table );
				
				queries[2] = malloc(1024);
				if (queries[2] == NULL) { error("ERROR allocating 3rd query buffer");};
				 snprintf(queries[2], 1024,
					"INSERT INTO %s (`%s_id`, `time`, `created`) VALUES (@%s_id, FROM_UNIXTIME('%llu'), NOW());",
					event_table, device_table, device_table, index->time_stamp / 1000
				);
				
				queries[3] = malloc(1024);
				if (queries[3] == NULL) { error("ERROR allocating 4th query buffer");};
				snprintf(queries[3], 1024, "SET @%s_id = LAST_INSERT_ID();", event_table );
				
				int query_cnt = 4;
			
				for(s = 0; s < set_count; s++){
					
					queries[query_cnt] = malloc(1024 * 1024);
					if (queries[query_cnt] == NULL) { error("ERROR allocating query buffer");};
					
					if(sets[s].table != NULL){
						
						if(values[s][0] == NULL){
							free(queries[query_cnt]);
							
							continue;
						}else{
							
							char *cols = malloc(1024 * 1024);
							if (cols == NULL) { error("ERROR allocating columns buffer");};
							char *vals = malloc(1024 * 1024);
							if (vals == NULL) { error("ERROR allocating values buffer");};
							cols[0] = 0;
							vals[0] = 0;
							
							int f; for(f = 0; f < sets[s].field_cnt; f++){
								
								strncat(cols, "`", 1024 * 1024);
								strncat(cols, sets[s].fields[f].name, 1024 * 1024);
								strncat(cols, "`,", 1024 * 1024);
								
								strncat(vals, "'", 1024 * 1024);
								strncat(vals, values[s][f], 1024 * 1024);
								strncat(vals, "',", 1024 * 1024);
								
							}
							cols[strnlen(cols, 1024 * 1024) - 1] = 0;
							vals[strnlen(vals, 1024 * 1024) - 1] = 0;
							
							snprintf(queries[query_cnt], 1024 * 1024, "INSERT INTO `%s` (%s) VALUES (%s);", sets[s].table, cols, vals);
				
							void *ptr = realloc(queries[query_cnt], strnlen(queries[query_cnt], 1024 * 1024));
							if (ptr == NULL) { error("ERROR reallocating queries[query_cnt] buffer");};
						
							free(cols);
							free(vals);
							
							query_cnt++;
							
							queries[query_cnt] = malloc(1024 * 1024);
							if (queries[query_cnt] == NULL) { error("ERROR allocating query lastid buffer");};
							
							snprintf(queries[query_cnt], 1024 * 1024, "UPDATE `%s` SET `%s_id`=LAST_INSERT_ID() WHERE `id` = @%s_id;", event_table, sets[s].table, event_table);
							
							ptr = realloc(queries[query_cnt], strnlen(queries[query_cnt], 1024 * 1024));
							if (ptr == NULL) { error("ERROR reallocating queries[query_cnt] buffer");};
							
							query_cnt++;
						}
						
					}else{
						
						if(values[s][0] == NULL){
							free(queries[query_cnt]);
							
							continue;
						}else{
						
							char *set = malloc(1024 * 1024);
							if (set == NULL) { error("ERROR allocating sets buffer");};
							set[0] = 0;
							
							int f; for(f = 0; f < sets[s].field_cnt; f++){
								
								strncat(set, "`", 1024 * 1024);
								strncat(set, sets[s].fields[f].name, 1024 * 1024);
								strncat(set, "` = '", 1024 * 1024);
								
								strncat(set, values[s][f], 1024 * 1024);
								strncat(set, "',", 1024 * 1024);
								
							}
							set[strnlen(set, 1024 * 1024) - 1] = 0;
							
							snprintf(queries[query_cnt], 1024 * 1024, "UPDATE `%s` SET %s WHERE id = @%s_id;", event_table, set, event_table);
						
							void *ptr = realloc(queries[query_cnt], strnlen(queries[query_cnt], 1024 * 1024));
							if (ptr == NULL) { error("ERROR reallocating queries[query_cnt] buffer");};
							
							free(set);
							
							query_cnt++;
						}
						
					}
					
				}
				
				mysql_conn = mysql_init(NULL);
			
				/* Connect to database */
				if (!mysql_real_connect(mysql_conn, mysql_server, mysql_user, mysql_password, mysql_database, 0, NULL, 0)) {
				  error(mysql_error(mysql_conn));
				}
				
				int q; for(q = 0; q < query_cnt; q++){
					//printf("%s\n\n", queries[q]);
					
					if (mysql_query(mysql_conn, queries[q])) {
						error(mysql_error(mysql_conn));
					}
				}
				
				mysql_close(mysql_conn);
					 
			}
			
			// Send back last received index.
			
			struct Index *last_index = (struct Index*)(index_buf + ((header->event_count - 1) * sizeof(struct Index)));
			
			struct Response response;
			response.code = 0;
			
			response.last_log = last_index->time_stamp;
		   
			n = write(newsockfd, &response, sizeof(struct Response));
			if (n < 0) error("ERROR writing to socket");
			
			close(newsockfd);
			close(sockfd);
			
			printf("%i events from '%s' up to %llu \n", header->event_count, header->uuid, response.last_log);
			
			free(header_buf);
			free(index_buf);
			free(data_buf);
			
			return 0;
		 }else{
			close(newsockfd);
		 }
		 
		 if(kbhit()) break;
		 
	 }

	close(sockfd);
	 
	return 0; 
}

