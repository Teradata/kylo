use kylo;
Delimiter //
create procedure check_data_job_relationships()

begin

DECLARE output VARCHAR(4000) DEFAULT '';

DECLARE checkFeedName VARCHAR(50);
DECLARE feedName VARCHAR(50);

DECLARE cnt INT DEFAULT 0;

/* flag to determine if the cursor is complete */
DECLARE cursorDone INT DEFAULT 0;


DECLARE cur CURSOR FOR
SELECT distinct i.JOB_NAME as 'CHECK_FEED_NAME', x.STRING_VAL as 'FEED_NAME'
FROM BATCH_JOB_EXECUTION_PARAMS x
inner join (SELECT JOB_EXECUTION_ID FROM kylo.BATCH_JOB_EXECUTION_PARAMS
WHERE KEY_NAME = 'jobType'
AND STRING_VAL = 'CHECK') c on c.JOB_EXECUTION_ID = x.JOB_EXECUTION_ID
inner Join BATCH_JOB_EXECUTION e on e.JOB_EXECUTION_ID = x.JOB_EXECUTION_ID
inner join BATCH_JOB_INSTANCE i on e.JOB_INSTANCE_ID = i.JOB_INSTANCE_ID
WHERE x.KEY_NAME = 'feed';

DECLARE checkDataFeedsCur CURSOR FOR
SELECT distinct f.NAME
FROM BATCH_JOB_EXECUTION_PARAMS x
inner join (SELECT JOB_EXECUTION_ID FROM kylo.BATCH_JOB_EXECUTION_PARAMS
WHERE KEY_NAME = 'jobType'
AND STRING_VAL = 'CHECK') c on c.JOB_EXECUTION_ID = x.JOB_EXECUTION_ID
inner Join BATCH_JOB_EXECUTION e on e.JOB_EXECUTION_ID = x.JOB_EXECUTION_ID
inner join BATCH_JOB_INSTANCE i on e.JOB_INSTANCE_ID = i.JOB_INSTANCE_ID
inner join FEED f on f.id = i.FEED_ID;


DECLARE CONTINUE HANDLER FOR NOT FOUND SET cursorDone = 1;

OPEN cur;
read_loop: LOOP

    FETCH cur INTO checkFeedName,feedName;
    IF cursorDone THEN
        LEAVE read_loop;
    END IF;
   /**
   RELATE the feeds together
    */
    SELECT count(*)
    into cnt
    FROM FEED_CHECK_DATA_FEEDS chk
    INNER JOIN FEED f on f.id = chk.feed_id
    left join FEED c  on c.id = chk.check_data_feed_id
    where f.NAME = feedName
    AND c.NAME = checkFeedName;

    if(cnt = 0 ) then
   INSERT INTO FEED_CHECK_DATA_FEEDS(`FEED_ID`,`CHECK_DATA_FEED_ID`)
   SELECT f.ID, c.ID from FEED f, FEED c
   where f.NAME = feedName
   AND c.NAME = checkFeedName;
    end if;
END LOOP;
CLOSE cur;


SET cursorDone = 0;

OPEN checkDataFeedsCur;
read_loop: LOOP

    FETCH checkDataFeedsCur INTO checkFeedName;
    IF cursorDone THEN
        LEAVE read_loop;
    END IF;
    UPDATE FEED SET FEED_TYPE = 'CHECK'
    WHERE FEED.NAME = checkFeedName;

END LOOP;
CLOSE checkDataFeedsCur;



END//

-- Execute the procedure
call check_data_job_relationships();

-- Drop the procedure
drop procedure check_data_job_relationships;