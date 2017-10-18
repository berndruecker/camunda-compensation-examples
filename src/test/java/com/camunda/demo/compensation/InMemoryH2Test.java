package com.camunda.demo.compensation;

import org.apache.ibatis.logging.LogFactory;
import org.camunda.bpm.engine.runtime.ProcessInstance;
import org.camunda.bpm.engine.test.ProcessEngineRule;
import org.camunda.bpm.engine.variable.Variables;
import org.camunda.bpm.extension.process_test_coverage.junit.rules.TestCoverageProcessEngineRuleBuilder;
import org.camunda.bpm.engine.test.Deployment;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;

import static org.camunda.bpm.engine.test.assertions.ProcessEngineTests.*;
import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.List;

/**
 * Test case starting an in-memory database-backed Process Engine.
 */
public class InMemoryH2Test {

  @ClassRule
  @Rule
  public static ProcessEngineRule rule = TestCoverageProcessEngineRuleBuilder.create().build();

  static {
    LogFactory.useSlf4jLogging(); // MyBatis
  }

  @Before
  public void setup() {
    init(rule.getProcessEngine());
  }

  /**
   * Just tests if the process definition is deployable.
   */
  @Test
  @Deployment(resources = "mi-compensation.bpmn")
  public void testParsingAndDeployment() {
	  List<String> devices = new ArrayList<String>();
	  devices.add("device1");
    devices.add("device2");
    devices.add("fail");

    ProcessInstance processInstance = processEngine().getRuntimeService().startProcessInstanceByKey( //
	      "order", //
	      Variables.putValue("devices", devices));
    
    assertThat(processInstance) //
      .isEnded()
      .hasPassedInOrder("ServiceTask_ConfigureDevice", "ServiceTask_ConfigureDevice", "ServiceTask_ConfigureDevice")
      .hasPassed("Throw_compensation", "EndEvent_Compensated")
      .hasPassedInOrder("ServiceTask_CompensateConfiguration", "ServiceTask_CompensateConfiguration", "ServiceTask_CompensateConfiguration");
  }

}
