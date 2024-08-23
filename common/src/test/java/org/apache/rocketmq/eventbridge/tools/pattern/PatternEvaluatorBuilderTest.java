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

package org.apache.rocketmq.eventbridge.tools.pattern;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class PatternEvaluatorBuilderTest {

    @Test
    @Disabled
    public void build_ComplexPatter() {
        String pattern = "{\n" + "    \"source\": [\"mq\", \"ecs\", 123],\n"
            + "    \"aliyunregionid\": [\"cn-hangzhou\", {\"prefix\": \"cn-\"}],\n" + "    \"data\": {\n"
            + "        \"c-count\": {\n" + "            \"d-count\": [{\"numeric\": [\">\", 10]}],\n"
            + "            \"but\": [{\"anything-but\": \"started\"}],\n" + "            \"e-count\": {\n"
            + "                \"f-count\": [{\"exists\": false}]\n" + "            }\n" + "        },\n"
            + "        \"prefix\": [{\"prefix\": \"aliyun-\"}],\n"
            + "        \"suffix\": [{\"suffix\": \"-eventbridge\"}]\n" + "    }\n" + "}";

        final PatternEvaluator evaluator = PatternEvaluatorBuilder.build(pattern);

        final List<PatternEntry> specAttrPatternList = evaluator.getSpecAttrPatternList();
        assertThat(specAttrPatternList).hasSize(1);
        assertThat(specAttrPatternList.get(0)
            .getPatternName()).isEqualTo("source");
        assertThat(specAttrPatternList.get(0)
            .getPatternPath()).isEqualTo("$.source");
        assertThat(specAttrPatternList.get(0)
            .getConditionList()).hasSize(3);

        for (PatternCondition patternCondition : specAttrPatternList.get(0)
            .getConditionList()) {
            assertThat(patternCondition).isInstanceOf(EqualCondition.class);
        }

        final List<PatternEntry> extensionsAttrPatternList = evaluator.getExtensionsAttrPatternList();
        assertThat(extensionsAttrPatternList).hasSize(1);
        assertThat(extensionsAttrPatternList.get(0)
            .getPatternName()).isEqualTo("aliyunregionid");
        assertThat(extensionsAttrPatternList.get(0)
            .getPatternPath()).isEqualTo("$.aliyunregionid");
        assertThat(extensionsAttrPatternList.get(0)
            .getConditionList()).hasSize(2);

        assertThat(extensionsAttrPatternList.get(0)
            .getConditionList()
            .get(0)).isInstanceOf(EqualCondition.class);
        assertThat(extensionsAttrPatternList.get(0)
            .getConditionList()
            .get(1)).isInstanceOf(PrefixCondition.class);

        final List<PatternEntry> dataPatternList = evaluator.getDataPatternList();

        Map<String, String> patternPathMap = new HashMap<>();
        patternPathMap.put("d-count", "$.c-count.d-count");
        patternPathMap.put("but", "$.c-count.but");
        patternPathMap.put("f-count", "$.c-count.e-count.f-count");
        patternPathMap.put("prefix", "$.prefix");
        patternPathMap.put("suffix", "$.suffix");

        Map<String, Class<?>> patternConditionMap = new HashMap<>();
        patternConditionMap.put("d-count", NumericCondition.class);
        patternConditionMap.put("but", AnythingButCondition.class);
        patternConditionMap.put("f-count", ExistsCondition.class);
        patternConditionMap.put("prefix", PrefixCondition.class);
        patternConditionMap.put("suffix", SuffixCondition.class);

        assertThat(dataPatternList).hasSize(patternPathMap.size());
        for (PatternEntry patternEntry : dataPatternList) {
            assertThat(patternPathMap.get(patternEntry.getPatternName())).isEqualTo(patternEntry.getPatternPath());
            assertThat(patternEntry.getConditionList()).hasSize(1);
            assertThat(patternEntry.getConditionList()
                .get(0)).isInstanceOf(patternConditionMap.get(patternEntry.getPatternName()));
        }
    }

    @Test
    public void build_EmptyPattern() {
        String pattern = "{}";
        PatternEvaluatorBuilder.build(pattern);
    }

    @Test
    public void build_EmptyPatternValue() {
        Throwable exception = assertThrows(InvalidEventPatternException.class, () -> {
            String pattern = "{\"source\": []}";
            PatternEvaluatorBuilder.build(pattern);
        });
        assertTrue(exception.getMessage().contains(PatternErrorMessages.INVALID_PATTERN_VALUE + "[]"));
    }

    @Test
    public void build_UnSupportedJsonPattern() {
        Throwable exception = assertThrows(InvalidEventPatternException.class, () -> {
            String pattern = "abc";
            PatternEvaluatorBuilder.build(pattern);
        });
        assertTrue(exception.getMessage().contains(PatternErrorMessages.NON_SUPPORTED_JSON));
    }

    @Test
    public void build_UnSupportedJsonArray() {
        Throwable exception = assertThrows(InvalidEventPatternException.class, () -> {
            String pattern = "[123]";
            PatternEvaluatorBuilder.build(pattern);
        });
        assertTrue(exception.getMessage().contains(PatternErrorMessages.NON_SUPPORTED_JSON));
    }

    @Test
    public void build_InvalidJSONPattern() {
        Throwable exception = assertThrows(InvalidEventPatternException.class, () -> {
            String pattern = "[123{]";
            PatternEvaluatorBuilder.build(pattern);
        });
        assertTrue(exception.getMessage().contains(PatternErrorMessages.INVALID_JSON_STRING));
    }

    @Test
    public void build_InvalidPatternFieldValue() {
        Throwable exception = assertThrows(InvalidEventPatternException.class, () -> {
            String fieldVal = "abc";
            String pattern = String.format("{\"source\": \"%s\"}", fieldVal);
            PatternEvaluatorBuilder.build(pattern);
        });
        assertTrue(exception.getMessage().contains(PatternErrorMessages.INVALID_PATTERN_VALUE + fieldVal));
    }

    @Test
    @Disabled
    public void build_UnrecognizedFieldKey() {
        Throwable exception = assertThrows(InvalidEventPatternException.class, () -> {
            String unrecognizedKey = "unrecognizedKey";
            String pattern = String.format("{\"%s\": [123]}", unrecognizedKey);
            PatternEvaluatorBuilder.build(pattern);
        });
        assertTrue(exception.getMessage().contains(PatternErrorMessages.UNRECOGNIZED_PATTERN_KEY + unrecognizedKey));
    }

    @Test
    public void build_NoDataPatternKey() {
        Throwable exception = assertThrows(InvalidEventPatternException.class, () -> {
            String pattern = "{\"data\":{}}";
            PatternEvaluatorBuilder.build(pattern);
        });
        assertTrue(exception.getMessage().contains(PatternErrorMessages.NO_DATA_PATTERN_KEY));
    }

    @Test
    public void build_NoDataPatternKey_Complex() {
        Throwable exception = assertThrows(InvalidEventPatternException.class, () -> {
            String pattern = "{\"data\":{\"c-count\":[123],\"map-node\":{\"inner-node1\":[{\"prefix\":\"abc\"},\"def\"],"
                + "\"inner-node2\":{}}}}";
            PatternEvaluatorBuilder.build(pattern);
        });
        assertTrue(exception.getMessage().contains(PatternErrorMessages.NO_DATA_PATTERN_KEY));
    }

    @Test
    public void build_InvalidPatternFieldValue_Complex() {
        Throwable exception = assertThrows(InvalidEventPatternException.class, () -> {
            String fieldVal = "abc";
            String pattern = String.format("{\"data\": {\"key\": \"%s\"}}", fieldVal);
            PatternEvaluatorBuilder.build(pattern);
        });
        assertTrue(exception.getMessage().contains(PatternErrorMessages.INVALID_PATTERN_VALUE + fieldVal));
    }

    @Test
    public void build_NestedPatternValue() {
        Throwable exception = assertThrows(InvalidEventPatternException.class, () -> {
            String nestedKey = "source";
            String pattern = String.format("{\"%s\": [123, [234]]}", nestedKey);
            System.out.println(pattern);
            PatternEvaluatorBuilder.build(pattern);
        });
        assertTrue(exception.getMessage().contains(PatternErrorMessages.NESTED_PATTERN_VALUE + nestedKey));
    }

    @Test
    public void build_UnrecognizedPatternCondition() {
        Throwable exception = assertThrows(InvalidEventPatternException.class, () -> {
            String unrecognizedKey = "unrecognizedKey";
            String pattern = String.format("{\"source\": [{\"%s\": \"abc\"}]}", unrecognizedKey);
            PatternEvaluatorBuilder.build(pattern);
        });
        assertTrue(exception.getMessage().contains(PatternErrorMessages.UNRECOGNIZED_PATTERN_CONDITION + unrecognizedKey));
    }

    @Test
    public void build_EmptyPrefixCondition() {
        Throwable exception = assertThrows(InvalidEventPatternException.class, () -> {
            String pattern = "{\"source\": [{\"prefix\": \"\"}]}";
            PatternEvaluatorBuilder.build(pattern);
        });
        assertTrue(exception.getMessage().contains(PatternErrorMessages.EMPTY_PREFIX_CONDITION));
    }

    @Test
    public void build_InvalidPrefixCondition() {
        Throwable exception = assertThrows(InvalidEventPatternException.class, () -> {
            String pattern = "{\"source\": [{\"prefix\": [\"abc\"]}]}";
            PatternEvaluatorBuilder.build(pattern);
        });
        assertTrue(exception.getMessage().contains(PatternErrorMessages.INVALID_PREFIX_CONDITION));
    }

    @Test
    public void build_EmptySuffixCondition() {
        Throwable exception = assertThrows(InvalidEventPatternException.class, () -> {
            String pattern = "{\"source\": [{\"suffix\": \"\"}]}";
            PatternEvaluatorBuilder.build(pattern);
        });
        assertTrue(exception.getMessage().contains(PatternErrorMessages.EMPTY_SUFFIX_CONDITION));
    }

    @Test
    public void build_InvalidSuffixCondition() {
        Throwable exception = assertThrows(InvalidEventPatternException.class, () -> {
            String pattern = "{\"source\": [{\"suffix\": [\"abc\"]}]}";
            PatternEvaluatorBuilder.build(pattern);
        });
        assertTrue(exception.getMessage().contains(PatternErrorMessages.INVALID_SUFFIX_CONDITION));
    }

    @Test
    public void build_InvalidExistsCondition() {
        Throwable exception = assertThrows(InvalidEventPatternException.class, () -> {
            String pattern = "{\"source\": [{\"exists\": \"true\"}]}";
            PatternEvaluatorBuilder.build(pattern);
        });
        assertTrue(exception.getMessage().contains(PatternErrorMessages.INVALID_EXISTS_CONDITION));
    }

    @Test
    public void build_InvalidCidrCondition_Non_String() {
        Throwable exception = assertThrows(InvalidEventPatternException.class, () -> {
            String pattern = "{\"source\": [{\"cidr\": 12345}]}";
            PatternEvaluatorBuilder.build(pattern);
        });
        assertTrue(exception.getMessage().contains(PatternErrorMessages.INVALID_CIDR_CONDITION));
    }

    @Test
    public void build_InvalidCidrCondition_Non_IpV4() {
        Throwable exception = assertThrows(InvalidEventPatternException.class, () -> {
            String pattern = "{\"source\": [{\"cidr\": \"fefefef\"}]}";
            PatternEvaluatorBuilder.build(pattern);
        });
        assertTrue(exception.getMessage().contains(PatternErrorMessages.INVALID_CIDR_CONDITION));
    }

    @Test
    public void build_InvalidAnythingButCondition_EmptyNestedCondition() {
        Throwable exception = assertThrows(InvalidEventPatternException.class, () -> {
            String pattern = "{\"source\": [{\"anything-but\": {}}]}";
            PatternEvaluatorBuilder.build(pattern);
        });
        assertTrue(exception.getMessage().contains(PatternErrorMessages.INVALID_PATTERN_CONDITION));
    }

    @Test
    public void build_InvalidAnythingButCondition() {
        Throwable exception = assertThrows(InvalidEventPatternException.class, () -> {
            String pattern = "{\"source\": [{\"anything-but\": [[123]]}]}";
            PatternEvaluatorBuilder.build(pattern);
        });
        assertTrue(exception.getMessage().contains(PatternErrorMessages.INVALID_ANYTHING_BUT_CONDITION));
    }

    @Test
    public void build_InvalidNestedAnythingButCondition() {
        Throwable exception = assertThrows(InvalidEventPatternException.class, () -> {
            String pattern = "{\"source\": [{\"anything-but\": {\"cidr\": \"127.0.0.1\"}}]}";
            PatternEvaluatorBuilder.build(pattern);
        });
        assertTrue(exception.getMessage().contains(PatternErrorMessages.INVALID_NESTED_ANYTHING_BUT_CONDITION));
    }

    @Test
    public void build_InvalidNumericCondition() {
        Throwable exception = assertThrows(InvalidEventPatternException.class, () -> {
            String pattern = "{\"source\": [{\"numeric\": 123}]}";
            PatternEvaluatorBuilder.build(pattern);
        });
        assertTrue(exception.getMessage().contains(PatternErrorMessages.INVALID_NUMERIC_CONDITION));
    }

    @Test
    public void build_InvalidNumericConditionValue() {
        Throwable exception = assertThrows(InvalidEventPatternException.class, () -> {
            String pattern = "{\"source\": [{\"numeric\": [\">\", 1.0e10]}]}";
            PatternEvaluatorBuilder.build(pattern);
        });
        assertTrue(exception.getMessage().contains(PatternErrorMessages.INVALID_NUMERIC_CONDITION_VALUE));
    }

    @Test
    public void build_UnrecognizedNumericConditionValue() {
        Throwable exception = assertThrows(InvalidEventPatternException.class, () -> {
            String pattern = "{\"source\": [{\"numeric\": [1234, 1.0e10]}]}";
            PatternEvaluatorBuilder.build(pattern);
        });
        assertTrue(exception.getMessage().contains(PatternErrorMessages.UNRECOGNIZED_NUMERIC_CONDITION_VALUE));
    }

    @Test
    public void build_UnrecognizedNumericConditionExp() {
        Throwable exception = assertThrows(InvalidEventPatternException.class, () -> {
            String pattern = "{\"source\": [{\"numeric\": [\"===\", 1.0e8]}]}";
            PatternEvaluatorBuilder.build(pattern);
        });
        assertTrue(exception.getMessage().contains(PatternErrorMessages.UNRECOGNIZED_NUMERIC_CONDITION_EXP));
    }

    @Test
    @Disabled
    public void build_UnrecognizedAliyunExtension() {
        Throwable exception = assertThrows(InvalidEventPatternException.class, () -> {
            String key = "aliyununrecognizedKey";
            String pattern = String.format("{\"%s\": [\"value\"]}", key);
            PatternEvaluatorBuilder.build(pattern);
        });
        assertTrue(exception.getMessage().contains(PatternErrorMessages.UNRECOGNIZED_PATTERN_KEY + key));
    }

    @Test
    public void test_getTargetElementOfFilterPattern() {
        String sourcePattern = "{\n" + "  \"source\" : [ \"acs.oss\" ],\n"
            + "  \"type\" : [ \"oss:ObjectCreated:PostObject\", \"oss:ObjectCreated:PutObject\", \"oss:ObjectCreated:CopyObject\", \"oss:ObjectCreated:CompleteMultipartUpload\" ],\n"
            + "  \"subject\" : [ {\n"
            + "    \"prefix\" : \"acs:oss:cn-shanghai:1646030314736845:ali-hdsh-ai-label-online-bucket/\"\n" + "  } ]\n"
            + "}";
        String targetPattern = PatternEvaluatorBuilder.getTargetElementOfFilterPattern(sourcePattern, "source", "type");
        Assertions.assertEquals("{\"source\":[\"acs.oss\"],\"type\":[\"oss:ObjectCreated:PostObject\","
            + "\"oss:ObjectCreated:PutObject\",\"oss:ObjectCreated:CopyObject\","
            + "\"oss:ObjectCreated:CompleteMultipartUpload\"]}", targetPattern);

        Assertions.assertEquals("{}", PatternEvaluatorBuilder.getTargetElementOfFilterPattern("{}", "source", "type"));
    }
}
