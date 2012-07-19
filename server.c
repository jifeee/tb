/* A simple server in the internet domain using TCP
   The port number is passed as an argument */
#include <stdio.h>
#include <stdlib.h>
#include <time.h>
#include <string.h>
#include <unistd.h>
#include <signal.h>
#include <sys/types.h>
#include <sys/socket.h>
#include <inttypes.h>
#include <netinet/in.h>

#include <openssl/sha.h>

#include <mysql.h>

#define FIELD_TINYINT	0
#define FIELD_SMALLINT	1
#define FIELD_INT		2
#define FIELD_BIGINT	3
#define FIELD_FLOAT		10
#define FIELD_DOUBLE	11
#define FIELD_TEXT		20

#define MAX_SETCOUNT		3
#define MAX_FIELDCOUNT		8
#define MAX_VALUELENGTH		1024
#define MAX_QUERYLENGTH		1024 * 1024
#define MAX_DATASIZE		1024 * 1024

#define SERVER_PORT	12348

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

char *device_table = "phones_log";
char *event_table = "events";

struct Set sets[] = {
	{.name = "state", .table = NULL, .fields = { 
		{ .name = "screen",		.type = FIELD_TINYINT}, 
		{ .name = "bluetooth",		.type = FIELD_TINYINT}, 
		{ .name = "gps",		.type = FIELD_TINYINT}, 
		{ .name = "locked",		.type = FIELD_TINYINT}, 
		{ .name = "alert",		.type = FIELD_TINYINT},	
		{ .name = "textbuster_mac",	.type = FIELD_TEXT}
	}, .field_cnt = 6},

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
		{ .name = "event",	.type = FIELD_TEXT}
	}, .field_cnt = 3},
};
int set_count = 2;

struct Header{
	uint16_t	magic;
	uint16_t	version;
	uint32_t	event_count;
	uint32_t	payload_size;
	uint8_t	uuid[68];
	uint8_t	set_hash[20];
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
char *mysql_user = "textbuster";
char *mysql_password = "asv1T8s8t"; /* set me first */
char *mysql_database = "textbuster";

void error(const char *msg)
{
    printf("ERROR %s\n", msg);
}

void error_a(const char *msg)
{
    error(msg);
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

// Socket globals;

int sockfd, newsockfd, portno = SERVER_PORT;
socklen_t clilen;
	
struct sockaddr_in serv_addr, cli_addr;

void error_r(int type){
	struct Response response = {type, 0};
		   
	int n = write(newsockfd, &response, sizeof(struct Response));
	if (n < 0) error_a("writing to socket");
			
	close(newsockfd);
	close(sockfd);
	
	const char *state_msg[] = {
		"OK", 
		"Internal service error",
		"Reading socket failed",
		"MySQL error",
		"5",
		"6",
		"7",
		"8",
		"9",
		"Set mismatch",
		"Illegal data size"
	};
	
	char msg[64];
	snprintf(msg, 64, "Server reported: %s\n", state_msg[type]);
	perror(msg);		
	exit(2);
}

int main(int argc, char *argv[]){

	printf("+++++++++++++++++++++\n+ CONFIG\n+++++++++++++++++++++\n\n");
	
	printf("MYSQL\n=====\n Server   : %s\n Database : %s\n User     : %s\n\n", mysql_server, mysql_database, mysql_user);
	
	printf("STRUCTS\n=======\n");
	printf(" Header   : %"PRIuPTR"\n", sizeof(struct Header) );
	printf(" Index    : %"PRIuPTR"\n", sizeof(struct Index) );
	printf(" Response : %"PRIuPTR"\n", sizeof(struct Response) );
	printf("\n");
	
	printf("DATATYPES\n=========\n");
	printf(" TINYINT  : %"PRIuPTR"\n", sizeof(char));
	printf(" SMALLINT : %"PRIuPTR"\n", sizeof(uint16_t));
	printf(" INT      : %"PRIuPTR"\n", sizeof(uint32_t));
	printf(" BIGINT   : %"PRIuPTR"\n", sizeof(uint64_t));
	printf(" FLOAT    : %"PRIuPTR"\n", sizeof(float));
	printf(" DOUBLE   : %"PRIuPTR"\n", sizeof(double));
	printf("\n");
	
	printf("MAXIMAL SIZES\n=========\n");
	printf(" MAX_DATASIZE : %i\n", MAX_DATASIZE);
	printf("\n");
	
	char set_fingerprint[1024] = "";
	char set_fingerprint_tmp[16];
	
	printf("DEFINITIONS\n===========\n");
	
	printf(" LOGSET_DEFINITIONS = {\n");
	int s; for(s = 0; s < set_count; s++){
	 printf("\t{\"%s\", ", sets[s].name);
	 
	 snprintf(set_fingerprint_tmp, 16, "[%i] ", s);
	 strncat(set_fingerprint, set_fingerprint_tmp, 1024);
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
		
		snprintf(set_fingerprint_tmp, 16, "%i ", sets[s].fields[f].type);
		strncat(set_fingerprint, set_fingerprint_tmp, 1024);
	 }
	 printf("}");
	 if(s + 1 != set_count) printf(",");
	 printf("\n");
	}
	printf("};\n\n");
	
	unsigned char set_hash[SHA_DIGEST_LENGTH];
	SHA1((unsigned char*)set_fingerprint, strnlen(set_fingerprint, 1024), set_hash);
	
	printf("FINGERPRINT\n===========\n");
	
	printf(" Set Fingerprint: \"%s\"\n", set_fingerprint);
	printf(" Set Hash       : \"");
	int i; for(i = 0; i < SHA_DIGEST_LENGTH; i++) printf("%X", set_hash[i]);
	printf("\"\n\n");
	
	// Open local service socket.
	
	int n;
	
	sockfd = socket(AF_INET, SOCK_STREAM, 0);
	
	int optval = 1;
	setsockopt(sockfd, SOL_SOCKET, SO_REUSEADDR, &optval, sizeof optval);
	
	if (sockfd < 0) error_a("opening socket");
	
	bzero((char *) &serv_addr, sizeof(serv_addr));
	
	
	serv_addr.sin_family = AF_INET;
	serv_addr.sin_addr.s_addr = INADDR_ANY;
	serv_addr.sin_port = htons(portno);
	
	if (bind(sockfd, (struct sockaddr *) &serv_addr,
		  sizeof(serv_addr)) < 0) 
		  error_a("on binding");
	listen(sockfd, 5);
	clilen = sizeof(cli_addr);
	
	
	printf("+++++++++++++++++++++\n+ RUNTIME\n+++++++++++++++++++++\n\n");
	
	// Prevent zombie process from fork.
	signal(SIGCHLD, SIG_IGN);
	
	while(1){
		 newsockfd = accept(sockfd, (struct sockaddr *) &cli_addr, &clilen);
		 
		 pid_t pID = fork();
		 if (pID == 0){
			
			char* header_buf = malloc(sizeof(struct Header));
			if(header_buf == NULL){ error("allocation header buffer"); error_r(1); }
			
			if (newsockfd < 0){ error("reading header"); error("on accept"); };
			  
			n = recv(newsockfd, header_buf, sizeof(struct Header), MSG_WAITALL);
			
			if (n != sizeof(struct Header)) { error("reading header"); error_r(1); };
			
			// Read Header.
			
			struct Header *header = (struct Header*)header_buf;
			header->uuid[65] = 0;
		   
			// Compare set_hash from header with the one of the server.
			
			if(memcmp(set_hash, header->set_hash, SHA_DIGEST_LENGTH) != 0){
				error("Set mismatch"); error_r(10);
			}
			
			if(header->payload_size > MAX_DATASIZE){
				error("Illegal data size"); error_r(11);
			}
			
			//printf("Header - magic(0x%X), version(%i), events(%i), payload(%i bytes), imei(%s)\n", header->magic, header->version, header->event_count, header->payload_size, header->uuid);
			
			// Read indexes.
			
			char* index_buf = malloc(sizeof(struct Index) * header->event_count);
			if(index_buf == NULL){ error("allocating index buffer"); error_r(1); }
			
			char* data_buf = malloc(MAX_DATASIZE);
			if(data_buf == NULL){ error("allocating data buffer"); error_r(1); }
			
			n = recv(newsockfd, index_buf, sizeof(struct Index) * header->event_count, MSG_WAITALL);
			
			if (n != sizeof(struct Index) * header->event_count) { error("reading index"); error_r(2); };
			
			int i; for(i = 0; i < header->event_count; i++){
				 
				struct Index *index = (struct Index*)(index_buf + (i * sizeof(struct Index)));
				 
				//printf(" Event - time(%llu) data(%i bytes)\n", index->time_stamp, index->data_size);
				
				int n = recv(newsockfd, data_buf, index->data_size, MSG_WAITALL);
			
				if (n != index->data_size) { error("reading data"); error_r(2); };
				
				char *values[set_count][MAX_FIELDCOUNT];
				
				// Loop through sets to load data payload from socket.
				
				int a = set_count / 8 + 1;
				int s; for(s = 0; s < set_count; s++){
					
					char bitfield = data_buf[s / 8];
					if(bitfield & 1 << (s % 8)){
						
						// Loop through fields.
						
						int f; for(f = 0; f < sets[s].field_cnt; f++){
							
							char *value = malloc(MAX_VALUELENGTH + 1);
							if (value == NULL) { error("allocating value buffer"); error_r(1); };
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
								snprintf(value, MAX_VALUELENGTH, "%" PRIu64, *(uint64_t*)(data_buf + a));
								a += 8;
							}else if(sets[s].fields[f].type == FIELD_FLOAT){
								snprintf(value, MAX_VALUELENGTH, "%f", *(float*)(data_buf + a));
								a += 4;
							}else if(sets[s].fields[f].type == FIELD_DOUBLE){
								snprintf(value, MAX_VALUELENGTH, "%F", *(double*)(data_buf + a));
								a += 8;
							}else if(sets[s].fields[f].type == FIELD_TEXT){
								mysql_escape_string(value, (char*)(data_buf + a), strnlen((char*)(data_buf + a), MAX_VALUELENGTH));
								a += strnlen(value, MAX_VALUELENGTH) + 1;
							}
							
							int value_len = strnlen(value, MAX_VALUELENGTH);
							
							if(value_len > 0){
								void *ptr = realloc(value, value_len);
								
								if (ptr == NULL) { error("reallocating value buffer"); error_r(1); };
								
								values[s][f] = value;
							}else{
								values[s][f] = "";
							}
							
							
							
						}
						
					}else{
						
						values[s][0] = NULL;
						
					}
					
				}
				
				// Loop through sets to process values.
				
				char *queries[2 + set_count];
				
				
				queries[0] = malloc(1024);
				if (queries[0] == NULL) { error("allocating 1st query buffer"); error_r(1); };
				 snprintf(queries[0], 1024,
					"INSERT INTO `%s` (`imei`, `created`, `seen`) VALUES ('%s', NOW(), NOW()) ON DUPLICATE KEY UPDATE `id` = LAST_INSERT_ID(`id`), `seen` = NOW();",
					device_table, header->uuid
				);
				
				queries[1] = malloc(1024);
				if (queries[1] == NULL) { error("allocating 2nd query buffer"); error_r(1); };
				snprintf(queries[1], 1024, "SET @%s_id = LAST_INSERT_ID();", device_table );
				
				queries[2] = malloc(1024);
				if (queries[2] == NULL) { error("allocating 3rd query buffer"); error_r(1); };
				 snprintf(queries[2], 1024,
					"INSERT INTO %s (`%s_id`, `time`, `created`) VALUES (@%s_id, FROM_UNIXTIME('%"PRIu64"'), NOW());",
					event_table, device_table, device_table, index->time_stamp / 1000
				);
				
				queries[3] = malloc(1024);
				if (queries[3] == NULL) { error("allocating 4th query buffer"); error_r(1); };
				snprintf(queries[3], 1024, "SET @%s_id = LAST_INSERT_ID();", event_table );
				
				int query_cnt = 4;
			
				for(s = 0; s < set_count; s++){
					
					queries[query_cnt] = malloc(MAX_QUERYLENGTH);
					if (queries[query_cnt] == NULL) { error("allocating query buffer"); error_r(1); };
					
					if(sets[s].table != NULL){
						
						if(values[s][0] == NULL){
							free(queries[query_cnt]);
							
							continue;
						}else{
							
							char *cols = malloc(MAX_QUERYLENGTH);
							if (cols == NULL) { error("allocating columns buffer"); error_r(1); };
							char *vals = malloc(MAX_QUERYLENGTH);
							if (vals == NULL) { error("allocating values buffer"); error_r(1); };
							cols[0] = 0;
							vals[0] = 0;
							
							int f; for(f = 0; f < sets[s].field_cnt; f++){
								
								strncat(cols, "`", MAX_QUERYLENGTH);
								strncat(cols, sets[s].fields[f].name, MAX_QUERYLENGTH);
								strncat(cols, "`,", MAX_QUERYLENGTH);
								
								strncat(vals, "'", MAX_QUERYLENGTH);
								strncat(vals, values[s][f], MAX_QUERYLENGTH);
								strncat(vals, "',", MAX_QUERYLENGTH);
								
							}
							cols[strnlen(cols, MAX_QUERYLENGTH) - 1] = 0;
							vals[strnlen(vals, MAX_QUERYLENGTH) - 1] = 0;
							
							snprintf(queries[query_cnt], MAX_QUERYLENGTH, "INSERT INTO `%s` (%s) VALUES (%s);", sets[s].table, cols, vals);
				
							void *ptr = realloc(queries[query_cnt], strnlen(queries[query_cnt], MAX_QUERYLENGTH));
							if (ptr == NULL) { error("reallocating queries[query_cnt] buffer"); error_r(1); };
						
							free(cols);
							free(vals);
							
							query_cnt++;
							
							queries[query_cnt] = malloc(MAX_QUERYLENGTH);
							if (queries[query_cnt] == NULL) { error("allocating query lastid buffer"); error_r(1); };
							
							snprintf(queries[query_cnt], MAX_QUERYLENGTH, "UPDATE `%s` SET `%s_id`=LAST_INSERT_ID() WHERE `id` = @%s_id;", event_table, sets[s].table, event_table);
							
							ptr = realloc(queries[query_cnt], strnlen(queries[query_cnt], MAX_QUERYLENGTH));
							if (ptr == NULL) { error("reallocating queries[query_cnt] buffer"); error_r(1); };
							
							query_cnt++;
						}
						
					}else{
						
						if(values[s][0] == NULL){
							free(queries[query_cnt]);
							
							continue;
						}else{
						
							char *set = malloc(MAX_QUERYLENGTH);
							if (set == NULL) { error("allocating sets buffer"); error_r(1); };
							set[0] = 0;
							
							int f; for(f = 0; f < sets[s].field_cnt; f++){
								
								strncat(set, "`", MAX_QUERYLENGTH);
								strncat(set, sets[s].fields[f].name, MAX_QUERYLENGTH);
								strncat(set, "` = '", MAX_QUERYLENGTH);
								
								strncat(set, values[s][f], MAX_QUERYLENGTH);
								strncat(set, "',", MAX_QUERYLENGTH);
								
							}
							set[strnlen(set, MAX_QUERYLENGTH) - 1] = 0;
							
							snprintf(queries[query_cnt], MAX_QUERYLENGTH, "UPDATE `%s` SET %s WHERE id = @%s_id;", event_table, set, event_table);
						
							void *ptr = realloc(queries[query_cnt], strnlen(queries[query_cnt], MAX_QUERYLENGTH));
							if (ptr == NULL) { error("reallocating queries[query_cnt] buffer"); error_r(1); };
							
							free(set);
							
							query_cnt++;
						}
						
					}
					
				}
				
				mysql_conn = mysql_init(NULL);
			
				/* Connect to database */
				if (!mysql_real_connect(mysql_conn, mysql_server, mysql_user, mysql_password, mysql_database, 0, NULL, 0)) {
				  error(mysql_error(mysql_conn));
				  error_r(3); 
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
			
			struct Response response = {0, last_index->time_stamp};
		   
			n = write(newsockfd, &response, sizeof(struct Response));
			if (n < 0){ error_a("writing to socket"); }
			
			close(newsockfd);
			close(sockfd);
			
			printf("%i events from '%s' up to %"PRIu64" \n", header->event_count, header->uuid, response.last_log);
			
			free(header_buf);
			free(index_buf);
			free(data_buf);
			
			exit(0);
		 }else{
			close(newsockfd);
		 }
		 
		 if(kbhit()) break;
		 
	 }

	close(sockfd);
	 
	exit(0); 
}

