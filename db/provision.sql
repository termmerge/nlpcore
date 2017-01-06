CREATE TABLE consumer (
  id UUID NOT NULL UNIQUE,
  username VARCHAR(30) NOT NULL UNIQUE, 
  created_at TIMESTAMP NOT NULL,
  PRIMARY KEY (id)
);
COMMENT ON TABLE consumer IS 'Users of TermMerge Service';

CREATE TABLE task(
  id UUID NOT NULL UNIQUE,
  consumer_id UUID REFERENCES consumer(id), 
  description VARCHAR(50) NOT NULL,
  created_at TIMESTAMP NOT NULL,
  destroyed_at TIMESTAMP NOT NULL,
  PRIMARY KEY (id)
);
COMMENT ON TABLE task IS 'Individual tasks submitted by a user for TermMerge computation';

CREATE TABLE task_parameter(
  task_id UUID NOT NULL REFERENCES task (id),
  parameter_key TEXT NOT NULL,
  parameter_value TEXT NOT NULL,
  PRIMARY KEY(task_id, parameter_key, parameter_value)
);
COMMENT ON TABLE task_parameter IS 'Parameters for some individual task';
