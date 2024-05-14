# Workflow Engine

The Workflow Engine is a module designed to handle flows, tasks, and their execution within a Java/Spring Boot application.

## Overview

The Workflow Engine manages the execution of predefined flows consisting of tasks. It orchestrates the sequence of tasks within a process and handles retries and error handling.

## Features

- **Process Management:** Initiating, managing, and tracking the execution of processes.
- **Task Execution:** Executing individual tasks within a process based on predefined flows.
- **Event-Driven Architecture:** Utilizing events for task execution and process progression.
- **Validation and Configuration:** Ensuring the integrity and validity of configured flows and tasks.

## Getting Started

### Prerequisites

- Java 17 or higher
- Maven
- PostgreSQL or compatible database

### Installation

1. Clone the repository: git clone https://bitbucket.devnet.klm.com/scm/dora/workflow-engine.git
2. Navigate to the project directory: cd workflow-engine 
3. Build the project: mvn clean install 
4. Configure the application properties for your database connection.

### Usage

1. Start the application. 
2. The ConfigurationLoader class validates the integrity of flows on application startup. 
3. Use the WorkflowEngineService methods to interact with the workflow engine.

### Configuration

#### Configuring the project

Database Configuration : Set up your database connection details in application.properties.
Flow definition : Create the flow and tasks, associate them with flowTasks
Task Implementation: Create task implementations by extending the TaskDelegate class.

#### Configuring flows

First of all, lets take a look into the flows definitions.
For our example we will define a flow that rebook a passenger from a flight A to a flight B.

A flow is defined by its name, with our example we can imagine a flow named `rebook-a-b`.
This flow should run for example 3 tasks : 
- `GetPassengerInfo` : This task will get the passenger information on the flight A in order to rebook it on the other flight
- `RebookPassenger` : This task will rebook the passenger to the flight B
- `CancelSegment` : This task will cancel the flight A since it has been rebooked on the flight B

Let assume that in this flow, we have to rebook a passenger, our two first step are mandatory, but maybe not the third one. It would mean that
our passenger will be safe taking his other flight, but maybe in our system, the first flight will not be cancelled if the task fail. For this example
we will not consider it as mandatory.

The SQL definition for this flow will be the following :

```sql
INSERT INTO config_workflow_flow (name)
VALUES
    ('rebook-a-b');

INSERT INTO config_workflow_task (name, description)
VALUES
    ('GetPassengerInfo', 'Getting passenger information'),
    ('RebookPassenger', 'Rebooking passenger on new flight'),
    ('CancelSegment', 'Cancelling previous flight');

INSERT INTO config_workflow_flow_task (allow_to_fail, task_order, flow_id, task_id, enabled)
VALUES
    (false, 1, 1, 1, true),
    (false, 2, 1, 2, true),
    (true, 3, 1, 3, true);
```

Now, we will have to define a specific context for our flow since we will need some variables shifting from one task to another, such as
the passenger reservation number (PNR), the flight number A and the flight number B. To do this we can inherit an entity from ContextEntity.

Here is an example :

```java
import com.sebastientr.workflow.domain.entity.ContextEntity;

@Getter
@Setter
@Entity
@Table(name = "workflow_context_rebook")
public class ContextRebookEntity extends ContextEntity {
    private String pnr;
    private String flightNumberSource;
    private String flightNumberTarget;
}
```

Now that we have a defined flow, and a custom context, we can implement our tasks that will call/run some business logic :

```java

import com.sebastientr.workflow.task.TaskDelegate;

class GetPassengerInfo extends TaskDelegate {
    private final PassengerInfoService passengerInfoService;

    public PassengerInfoService getPassengerInfoService() {
        return passengerInfoService;
    }

    @Override
    public void execute(ContextRebookEntity context) {
        passengerInfoService.getPassengerInfo(context);
    }
}
```

So the tasks inherit from the `TaskDelegate` abstract class, this will allow the workflow engine to find them and execute the overrided `.execute()` method

#### Running flows

Now that we have a well configured flow, how to run it ? 

To run a flow, we have to call the `start()` method in `workflowEngineService` :

```java
import com.sebastientr.workflow.domain.entity.ProcessEntity;

class MyService {
    private final IWorkflowEngineService workflowEngineService;

    public ProcessEntity startNewProcess() {
        workflowEngineService.start(flowName, context);
    }
}
```

You can see here that when we start a flow, we retrieve a `ProcessEntity`.

A `ProcessEntity` is in fact an instantiation of a flow. And the instantiation of a task is a `TaskInstanceEntity`.

A process entity contains the name of the flow, the status (INIT, IN_PROGRESS, SUCCESS, ERROR, WARNING), and a few other useful information

A task instance entity contains the name of the task, its status (IN_PROGRESS, SUCCESS, ERROR, RETRIED) and some other information.

#### Plugging listeners

Since flows are asynchronous, we do start them but do not know when they will finish.

In some applications, different actions has to be implemented at the beginning and the end of processes and tasks. So lets create a consumer
that will be able to handle those cases :

```java
import lombok.extern.slf4j.Slf4j;

@Slf4j
class ConsumerService {
    @Async
    @EventListener(condition = "{event.type == T(com.sebastientr.workflow.queuing.WorkflowEvent.EventType).START_TASK}")
    public void handleStartTaskEvent(@NotNull WorkflowEvent<StartTaskDTO> event) {
        log.info("Received {}/{} event", event.getType(), event.getWhat().getFlowTask().getTask().getName());
    }

    @Async
    @EventListener(condition = "{event.type == T(com.sebastientr.workflow.queuing.WorkflowEvent.EventType).START_PROCESS}")
    public void handleStartProcessEvent(@NotNull WorkflowEvent<StartProcessDTO> event) {
        log.info("Received {} event", event.getType());
    }

    @Async
    @EventListener(condition = "{event.type == T(com.sebastientr.workflow.queuing.WorkflowEvent.EventType).END_TASK}")
    public void handleEndTaskEvent(@NotNull WorkflowEvent<EndTaskDTO> event) {
        log.info("Received {}/{} event with status {}", event.getType(), event.getWhat().getFlowTask().getTask().getName(), event.getWhat().getTaskInstance().getStatus());
    }

    @Async
    @EventListener(condition = "{event.type == T(com.sebastientr.workflow.queuing.WorkflowEvent.EventType).END_PROCESS}")
    public void handleEndProcessEvent(@NotNull WorkflowEvent<EndProcessDTO> event) {
        log.info("Received {} event", event.getType());
    }
}
```

4 types of events are published in the execution of a flow :
- `START_TASK` : when task is starting
- `END_TASK` : when a task is finished
- `START_PROCESS` : when a process is started
- `END_PROCESS` : when a process is finished

Each task has its own DTO defined in `com.sebastientr.workflow.dto.event`, please read the classes for more information.

