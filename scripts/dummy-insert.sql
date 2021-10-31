select * from resource_discovery;
select * from agent;
select * from agent_registration;
select * from resource;
select * from task;
select * from task_handling_agent
--INSERT INTO resource_discovery (resource_uri, task_uri) VALUES ('http://localhost:8080/axis/resourceService.wsdl', 'http://localhost:8080/axis/taskService.wsdl');
--INSERT INTO agent (description, resource_discovery_id, uuid) VALUES ('default agent', 1, '26853a29-0073-4ab8-b3ad-5aacf442d0f5');
--INSERT INTO agent_registration (agent_id) VALUES  (1);
--INSERT INTO resource (uri,description) VALUES ('http://localhost:8080/axis/resourceService1','test resource 1');
--INSERT INTO task (original_agent_id, description, executable_location, dependencies) VALUES (1, 'test task 1', '/home/marc/test','');
--INSERT INTO task (original_agent_id, description, executable_location, dependencies) VALUES (1, 'test task 2', '/home/marc/test2','');
--INSERT INTO task (original_agent_id, description, executable_location, dependencies) VALUES (1, 'test task 2', '/home/marc/test3','');
--INSERT INTO task (original_agent_id, description, executable_location, dependencies) VALUES (1, 'test task 2', '/home/marc/test4','');
--INSERT INTO task_handling_agent (task_id, agent_id) VALUES (1,1);
--INSERT INTO task_handling_agent (task_id, agent_id) VALUES (2,1);
--INSERT INTO task_handling_agent (task_id, agent_id) VALUES (3,1);
--INSERT INTO task_handling_agent (task_id, agent_id) VALUES (4,1);
