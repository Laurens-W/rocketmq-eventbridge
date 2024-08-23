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

package org.apache.rocketmq.eventbridge.domain.service;

import org.apache.rocketmq.eventbridge.domain.model.rule.EventRuleService;
import org.apache.rocketmq.eventbridge.domain.model.run.EventTargetRunnerService;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

@MockitoSettings(strictness = Strictness.WARN)
public class EventRuleDomainServiceTest {
    @InjectMocks
    private EventRuleDomainService eventRuleDomainService;
    @InjectMocks
    private EventTargetRunnerService eventTargetRunnerService;
    @InjectMocks
    private EventRuleService eventRuleService;

    @Test
    public void getEventRuleDetail() {
    }

    @Test
    public void deleteEventRuleWithDependencies() {
    }

    @Test
    public void updateEventRuleWithDependencies() {
    }

    @Test
    public void disableEventRuleWithDependencies() {
    }

    @Test
    public void enableEventRuleWithDependencies() {
    }
}