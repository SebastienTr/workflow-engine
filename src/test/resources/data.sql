INSERT INTO config_workflow_flow (name)
VALUES
    ('test-flow');

INSERT INTO config_workflow_task (name, description)
VALUES
    ('FirstTestTask', 'Running first test task'),
    ('SecondTestTask', 'Running second test task'),
    ('ThirdTestTask', 'Running third test task');

INSERT INTO config_workflow_flow_task (allow_to_fail, task_order, flow_id, task_id, enabled)
VALUES
    (false, 1, 1, 1, true),
    (true, 2, 1, 2, true),
    (true, 3, 1, 3, true);
