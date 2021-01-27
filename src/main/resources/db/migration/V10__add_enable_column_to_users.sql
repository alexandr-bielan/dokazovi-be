ALTER TABLE PUBLIC.USERS ADD ENABLED BOOLEAN;

CREATE TABLE PUBLIC.VERIFICATION_TOKENS (
                              ID SERIAL PRIMARY KEY,
                              TOKEN VARCHAR,
                              USER_ID INT
);

ALTER TABLE PUBLIC.VERIFICATION_TOKENS ADD FOREIGN KEY (USER_ID) REFERENCES PUBLIC.USERS (USER_ID);