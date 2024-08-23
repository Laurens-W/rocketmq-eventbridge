/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.rocketmq.eventbridge.domain.model;

import com.google.common.collect.Maps;
import com.google.gson.Gson;
import java.util.Map;
import org.apache.rocketmq.eventbridge.domain.model.classes.APIAttribute;
import org.apache.rocketmq.eventbridge.domain.model.classes.EventSourceClass;
import org.apache.rocketmq.eventbridge.domain.model.classes.EventSourceClassService;
import org.apache.rocketmq.eventbridge.domain.repository.EventSourceClassRepository;
import org.apache.rocketmq.eventbridge.exception.EventBridgeException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

@MockitoSettings(strictness = Strictness.WARN)
public class EventTargetRunnerClassServiceTest {

    @InjectMocks
    EventSourceClassService eventSourceClassService;

    @Mock
    EventSourceClassRepository eventSourceClassRepository;

    private String testSourceClassName = "acs.mns";

    @BeforeEach
    public void before() {
        Map<String, APIAttribute> apiParams = Maps.newHashMap();
        apiParams.put("RegionId", new APIAttribute("", "The source region id.", false, null));
        apiParams.put("QueueName", new APIAttribute("", "The queue name.", true, null));
        apiParams.put("IsBase64Encode", new APIAttribute("", "Base64 encode body or not.", true, null));

        Map<String, Object> requiredParams = Maps.newHashMap();
        requiredParams.put("Endpoint", "${AccountId}.mns.${RegionId}.aliyuncs.com");
        requiredParams.put("RoleName", "UserDefinedRoleName");
        requiredParams.put("QueueName", "${QueueName}");
        requiredParams.put("IsBase64Encode", "${IsBase64Encode}");

        Map<String, Object> transform = Maps.newHashMap();
        transform.put("data", "{\"value\":\"$.data\",\"form\":\"JSONPATH\"}");
        transform.put("subject",
            "{\"value\":\"acs:mns:${RegionId}:${AccountId}:queues/${QueueName}\",\"form\":\"CONSTANT\"}");
        transform.put("type", "{\"value\":\"mns.sendMsg\",\"form\":\"CONSTANT\"}");

        EventSourceClass eventSourceClass = EventSourceClass.builder()
            .apiParams(apiParams)
            .requiredParams(requiredParams)
            .transform(transform)
            .build();
        when(eventSourceClassRepository.getEventSourceClass(testSourceClassName)).thenReturn(eventSourceClass);

    }

    @Test
    public void checkEventSourceAPIParams_Pass() {
        Map<String, Object> inputConfig = Maps.newHashMap();
        inputConfig.put("QueueName", "demo");
        inputConfig.put("IsBase64Encode", true);
        eventSourceClassService.checkEventSourceAPIParams(testSourceClassName, inputConfig);
    }

    @Test
    public void checkEventSourceAPIParams_EventSourceMissingAttribute() {
        Throwable exception = assertThrows(EventBridgeException.class, () -> {
            Map<String, Object> inputConfig = Maps.newHashMap();
            inputConfig.put("QueueName", "demo");
            inputConfig.put("IsBase64Encode", true);
            inputConfig.put("InvalidAttribute", true);
            eventSourceClassService.checkEventSourceAPIParams(testSourceClassName, inputConfig);
        });
        assertTrue(exception.getMessage().contains("The attribute [InvalidAttribute] is ineffective, which effective attribute is [IsBase64Encode,RegionId,"
            + "QueueName]."));
    }

    @Test
    public void checkEventSourceAPIParams_EventSourceIneffectiveAttribute() {
        Throwable exception = assertThrows(EventBridgeException.class, () -> {
            Map<String, Object> inputConfig = Maps.newHashMap();
            inputConfig.put("IsBase64Encode", true);
            eventSourceClassService.checkEventSourceAPIParams(testSourceClassName, inputConfig);
        });
        assertTrue(exception.getMessage().contains("Missing the attribute [QueueName:The queue name.] "));
    }

    @Test
    public void testRenderConfig() {
        Map<String, Object> inputConfig = Maps.newHashMap();
        inputConfig.put("RegionId", "cn-hangzhou");
        inputConfig.put("QueueName", "demo");
        inputConfig.put("IsBase64Encode", true);
        Component component = eventSourceClassService.renderConfig("123456", testSourceClassName, inputConfig);
        System.out.println(new Gson().toJson(component));
        Assertions.assertEquals(testSourceClassName, component.getName());
        Assertions.assertEquals("123456.mns.cn-hangzhou.aliyuncs.com", component.getConfig()
            .get("Endpoint"));
        Assertions.assertEquals("UserDefinedRoleName", component.getConfig()
            .get("RoleName"));
        Assertions.assertEquals("demo", component.getConfig()
            .get("QueueName"));
        Assertions.assertEquals("true", component.getConfig()
            .get("IsBase64Encode"));
    }

    @Test
    public void renderCloudEventTransform() {
        Map<String, Object> inputConfig = Maps.newHashMap();
        inputConfig.put("RegionId", "cn-hangzhou");
        inputConfig.put("QueueName", "demo");
        inputConfig.put("IsBase64Encode", true);

        Map<String, Object> transform = eventSourceClassService.renderCloudEventTransform("123456", testSourceClassName,
            inputConfig, "eventSource");

        Assertions.assertEquals("{\"value\":\"$.data\",\"form\":\"JSONPATH\"}", transform.get("data"));
        Assertions.assertEquals("{\"value\":\"acs:mns:cn-hangzhou:123456:queues/demo\",\"form\":\"CONSTANT\"}",
            transform.get("subject"));
        Assertions.assertEquals("{\"form\":\"CONSTANT\",\"value\":\"eventSource\"}", transform.get("source"));
        Assertions.assertEquals("{\"value\":\"mns.sendMsg\",\"form\":\"CONSTANT\"}", transform.get("type"));
    }
}