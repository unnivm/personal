
CREATE TABLE bugs ( id INT, open_date CHAR(10), close_date CHAR(10), severity INT );
INSERT INTO bugs VALUES
(1, '02-01-2012', '03-01-2012', 1), (2, '02-01-2012', '03-01-2012', 1), (3, '03-01-2012', '04-01-2012', 2), (4, '04-01-2012', '04-01-2012', 2);

 SELECT * FROM bugs 
 WHERE open_date BETWEEN '02-01-2012' AND '04-01-2012'  
 AND (close_date is null || close_date = open_date);
