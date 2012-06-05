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

struct Package{
	char         	magic[2];
	unsigned short  version;
	char            imei[65];
	unsigned int    length;
};

struct Event{
	unsigned long long time;
	unsigned char      screen;
	unsigned char      bluetooth;
	unsigned char      gps;
};

char screen_str   [4][4] = { "OFF", "ON", "IL1", "IL2" };
char bluetooth_str[4][4] = { "NA", "OFF", "ON", "SCN" };
char gps_str      [4][4] = { "NA", "OFF", "ON", "NPOS" };

MYSQL *mysql_conn;
MYSQL_RES *mysql_res;
MYSQL_ROW mysql_row;
char *mysql_server = "192.168.0.46";
char *mysql_user = "root";
char *mysql_password = ""; /* set me first */
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

int main(int argc, char *argv[])
{
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
     listen(sockfd,5);
     clilen = sizeof(cli_addr);
     
     while(1){
	     newsockfd = accept(sockfd, 
	                 (struct sockaddr *) &cli_addr, 
	                 &clilen);
	     
	     pid_t pID = fork();
		 if (pID == 0){
			
			int header_length = 2 + 2 + 32 + 4;
			
			char* buffer = malloc(header_length);
		    if(buffer == NULL){ error("ERROR allocating header buffer"); }
			
		    if (newsockfd < 0){ error("ERROR on accept"); };
		      
		    n = read(newsockfd, buffer, header_length);
		     
		    if (n != header_length) { error("ERROR reading from heading");};
		    
		    struct Package pk;
		    
		    pk.magic[0] = buffer[0];
		    pk.magic[1] = buffer[1];
		    pk.version = *(short*)(buffer + 2);
		    sprintf(pk.imei, "%llX%llX%llX%llX",
				*(unsigned long long*)(buffer + 4),
				*(unsigned long long*)(buffer + 12),
				*(unsigned long long*)(buffer + 20),
				*(unsigned long long*)(buffer + 28));
			pk.length = *(unsigned int*)(buffer + 36);
			
			printf("%i - MAGIC: %c%c VER: %i LEN: %i DEV: %s\n", (int)time(NULL), pk.magic[0], pk.magic[1], pk.version, pk.length, pk.imei);
		    
		    
		    mysql_conn = mysql_init(NULL);
			
			/* Connect to database */
			if (!mysql_real_connect(mysql_conn, mysql_server,
				 mysql_user, mysql_password, mysql_database, 0, NULL, 0)) {
			  fprintf(stderr, "%s\n", mysql_error(mysql_conn));
			  exit(1);
			}
			
			char query[4096];
			sprintf(query,
				"INSERT INTO devices (imei, created, seen) VALUES ('%s', NOW(), NOW()) ON DUPLICATE KEY UPDATE iddevices=LAST_INSERT_ID(iddevices), seen=NOW();"
			, pk.imei );
			
			/* send SQL query */
			if (mysql_query(mysql_conn, query)) { fprintf(stderr, "SQLSEND - %s\n", mysql_error(mysql_conn)); exit(1);}
			
			/* send SQL query */
			if (mysql_query(mysql_conn, "SET @deviceid = LAST_INSERT_ID();")) { fprintf(stderr, "[ERR] %i - SQLSEND - %s\n", (int)time(NULL), mysql_error(mysql_conn)); exit(1); }
			
		   
		    buffer = malloc(pk.length);
		    if(buffer == NULL){ error("ERROR allocating payload buffer"); }
		    
		    int bytes_read = 0;
		    
		    while(n != 0 && bytes_read != pk.length){
				n = read(newsockfd, buffer + bytes_read, pk.length);
				if (n < -1){ error("ERROR reading payload"); }
				bytes_read += n;
				//printf("%i %i %i\n", pk.length, n, bytes_read);
			}    
		    
		    int event_count = 0;
		    n = 0;
		    while(n < pk.length){
			    struct Event ev;
			     
			    ev.time		 = *(unsigned long long*)(buffer + n);
			    n = n + 8;
			    
			    ev.screen 	 =  (*(char*)(buffer + n) >> 6) % 4;
			    ev.bluetooth =  (*(char*)(buffer + n) >> 4) % 4;
			    ev.gps 		 =  (*(char*)(buffer + n) >> 2) % 4;
			     
			    n++;
			    
			    event_count++;
			     
				sprintf(query,
					"INSERT INTO events (device, time, screen, bluetooth, gps) VALUES (@deviceid, FROM_UNIXTIME(%llu), '%s', '%s', '%s');"
				, ev.time / 1000, screen_str[ev.screen], bluetooth_str[ev.bluetooth], gps_str[ev.gps]);
				
				/* send SQL query */
				if (mysql_query(mysql_conn, query)) { fprintf(stderr, "[ERR] %i - SQLSEND - %s\n", (int)time(NULL), mysql_error(mysql_conn)); exit(1); }
			     
			    //printf(" %llu %s %s %s %X\n", ev.time, screen_str[ev.screen], bluetooth_str[ev.bluetooth], gps_str[ev.gps], *(char*)(buffer + n - 1));
			}
			
			printf("%i events stored\n", event_count);
			
		    mysql_close(mysql_conn);
			
			 
		    n = write(newsockfd,"OKko",4);
		    if (n < 0) error("ERROR writing to socket");
		    
		    close(newsockfd);
		    close(sockfd);
			
			printf("Done\n");
			
			free(buffer);
			
		    return 0;
		 }else{
			close(newsockfd);
		 }
		 
		 if(kbhit()) break;
	     
	 }

	close(sockfd);
     
    return 0; 
}

