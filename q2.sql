
DELIMITER $$
 
CREATE FUNCTION initcap(input char(255)) RETURNS VARCHAR(255)
    DETERMINISTIC
    
BEGIN
    DECLARE lvl varchar(10);
 
    DECLARE len int;
    
    DECLARE i int;
    
    SET len = CHAR_LENGTH(input);
    SET input = LOWER(input);
    SET i = 0;
    
    while (i < len ) DO
    IF (MID(input,i,1) = ' ' OR i = 0) THEN
    
    IF (i < len) THEN
				SET input = CONCAT(
					LEFT(input,i),
					UPPER(MID(input,i + 1,1)),
					RIGHT(input,len - i - 1)
				);
			END IF;
		END IF;
		SET i = i + 1;
	END WHILE;

	RETURN input;
END;    