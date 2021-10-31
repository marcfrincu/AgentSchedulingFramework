-- Schema: "public"

-- DROP SCHEMA public;

--
-- Tables required by the task service
--

-- defines a task
CREATE TABLE task (
	id serial PRIMARY KEY,
	original_agent_id integer CHECK (original_agent_id > 0),
	description text NOT NULL,
	executable_location character varying(255) NOT NULL,
	dependencies text NOT NULL,
	assigned_resource_id integer DEFAULT 0 CHECK (assigned_resource_id >= 0),
	position_on_resource_queue integer DEFAULT 0 CHECK (position_on_resource_queue >= 0),
	submission_time_local integer DEFAULT 0,
	submission_time_total integer DEFAULT 0,
	estimated_execution_time_on_resource integer DEFAULT 0 CHECK (estimated_execution_time_on_resource >= 0),
	estimated_completion_time_on_resource integer DEFAULT 0 CHECK (estimated_completion_time_on_resource >= 0),
	locked boolean DEFAULT false,
	CHECK (submission_time_total <= submission_time_local),
	CHECK (estimated_execution_time_on_resource <= estimated_completion_time_on_resource)
);

-- defines a task's handling agent
CREATE TABLE task_handling_agent (
	id serial PRIMARY KEY,
	task_id integer REFERENCES task(id) ON DELETE CASCADE,
	agent_id integer DEFAULT 1 CHECK (agent_id > 0)
);

--
-- Tables required by the resource service
--

-- defines a resource
CREATE TABLE resource (
	id serial PRIMARY KEY,
	uri character varying NOT NULL,
	description text NOT NULL,
	available boolean DEFAULT true,
	locked boolean DEFAULT false
);

-- defines the tasks allocated to a resource
CREATE TABLE resource_tasks (
	id serial PRIMARY KEY,
	task_id integer NOT NULL CHECK (task_id >= 0),
	resource_id integer REFERENCES resource(id) ON DELETE CASCADE,
	position_in_queue integer DEFAULT 0 CHECK (position_in_queue >= 0)
);

--
-- Tables required by the agent service
--

-- defines resource discovery entries used by the agent to access resource/task data
CREATE TABLE resource_discovery (
	id serial PRIMARY KEY,
	resource_uri character varying(255) NOT NULL,
	task_uri character varying(255) NOT NULL 
);

-- defines an agent
CREATE TABLE agent (
	id serial PRIMARY KEY,
	uuid character varying(255) NOT NULL,
	clone_of_agent_id integer DEFAULT -1,
	description text NOT NULL,
	resource_discovery_id integer REFERENCES resource_discovery(id) ON DELETE RESTRICT,
	available boolean DEFAULT true
);

CREATE TABLE agent_module (
	id serial PRIMARY KEY,
	last_ping numeric DEFAULT 0,
	status character varying(20) NOT NULL,
	agent_uuid character varying(255) NOT NULL,
	workflow_id character varying(255) NOT NULL,
	type character varying(255) NOT NULL,
	paused boolean DEFAULT true,
	external boolean DEFAULT false,
	start_script text NOT NULL,
	archive text NOT NULL
);

-- defines the task list allocated to an agent
CREATE TABLE agent_task_group (
	id serial PRIMARY KEY,
	requesting_agent_id integer REFERENCES agent(id) ON DELETE CASCADE,
	task_id integer NOT NULL CHECK (task_id > 0)
);

-- defines a group of agents
CREATE TABLE agent_group (
	id serial PRIMARY KEY,
	group_id integer DEFAULT 0,
	agent_id integer REFERENCES agent(id) ON DELETE CASCADE
);

-- defines a group of agents
CREATE TABLE healing_groups (
	id serial PRIMARY KEY,
	healing_agent_id integer REFERENCES agent(id) ON DELETE CASCADE
	module_id integer REFERENCES agent_module(id) ON DELETE CASCADE
);

-- defines an offer made by a bidding agent to a requesting agent
CREATE TABLE offer (
	id serial PRIMARY KEY,
	requesting_agent_id integer REFERENCES agent(id) ON DELETE CASCADE,
	bidding_agent_id integer REFERENCES agent(id) ON DELETE CASCADE,
	task_id integer NOT NULL CHECK (task_id > 0),
	value text NOT NULL
);

-- defines the registration of an agent
CREATE TABLE agent_registration (
	id serial PRIMARY KEY,
	agent_id integer REFERENCES agent(id) ON DELETE CASCADE
);

CREATE TABLE trace (
	id serial PRIMARY KEY,
	agent_uuid character varying(255) NOT NULL,
	workflow_id character varying(255) NOT NULL,
	module_type character varying(255) NOT NULL,
	msg_description text NOT NULL,
	msg_time numeric DEFAULT 0
);
