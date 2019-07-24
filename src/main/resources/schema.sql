CREATE TABLE IF NOT EXISTS document (
    id varchar(255) NOT NULL, 
    content oid NOT NULL, 
    content_length int8 NOT NULL, 
    content_type varchar(255) NOT NULL, 
    filename varchar(255) NOT NULL, 
PRIMARY KEY (id));
