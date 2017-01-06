CREATE OR REPLACE FUNCTION create_task_for_consumer(
  description VARCHAR(50),
  consumer_id UUID
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
  
  LOCK TABLE task IN ACCESS EXCLUSIVE MODE;
  
  WHILE same_uuid_count > 0 LOOP
    -- Generate a UUID for task (from uuid-oosp extension) 
    --  We chose this function because: 
    --  starkandwayne.com/blog/uuid-primary-keys-in-postgresql/
    SELECT uuid_generate_vlmc() INTO generated_uuid;
    
    SELECT COUNT(*) INTO same_uuid_count
    FROM task
    WHERE id=generated_uuid;
  END LOOP;
  
  INSERT INTO task(
    'id', 'consumer_id', 'description', 'created_at', 'destroyed_at'
  )
  VALUES (generated_uuid, consumer_uuid, creation_timestamp, NULL);
END;
$$ LANGUAGE plpgsql;
  