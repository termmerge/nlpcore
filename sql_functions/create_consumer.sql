CREATE OR REPLACE FUNCTION create_consumer(
  username VARCHAR(30)
)
RETURNS UUID
AS 
$$
DECLARE 
  generated_uuid UUID;
  same_uuid_count INTEGER = 1;
  creation_timestamp TIMESTAMP;
BEGIN
  SELECT current_timestamp() INTO creation_timestamp;
  
  LOCK TABLE consumer IN ACCESS EXCLUSIVE MODE;
  
  WHILE same_uuid_count > 0 LOOP
    -- Generate a UUID for consumer (from uuid-oosp extension) 
    --  We chose this function because: 
    --  starkandwayne.com/blog/uuid-primary-keys-in-postgresql/
    SELECT uuid_generate_v1mc() INTO generated_uuid;
    
    SELECT COUNT(*) INTO same_uuid_count 
    FROM consumer
    WHERE id=generated_uuid;
  END LOOP;
  
  INSERT INTO customer('id', 'username', 'created_at')
  VALUES (generated_uuid, username, creation_timestamp)
END;
$$ LANGUAGE plpgsql;
