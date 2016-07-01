begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to Elasticsearch under one or more contributor  * license agreements. See the NOTICE file distributed with  * this work for additional information regarding copyright  * ownership. Elasticsearch licenses this file to you under  * the Apache License, Version 2.0 (the "License"); you may  * not use this file except in compliance with the License.  * You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.ingest.common
package|package
name|org
operator|.
name|elasticsearch
operator|.
name|ingest
operator|.
name|common
package|;
end_package

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|ingest
operator|.
name|AbstractProcessor
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|ingest
operator|.
name|ConfigurationUtils
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|ingest
operator|.
name|IngestDocument
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|ingest
operator|.
name|Processor
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|HashMap
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|List
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|Map
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|Objects
import|;
end_import

begin_import
import|import static
name|org
operator|.
name|elasticsearch
operator|.
name|ingest
operator|.
name|ConfigurationUtils
operator|.
name|newConfigurationException
import|;
end_import

begin_class
DECL|class|GrokProcessor
specifier|public
specifier|final
class|class
name|GrokProcessor
extends|extends
name|AbstractProcessor
block|{
DECL|field|TYPE
specifier|public
specifier|static
specifier|final
name|String
name|TYPE
init|=
literal|"grok"
decl_stmt|;
DECL|field|PATTERN_MATCH_KEY
specifier|private
specifier|static
specifier|final
name|String
name|PATTERN_MATCH_KEY
init|=
literal|"_ingest._grok_match_index"
decl_stmt|;
DECL|field|matchField
specifier|private
specifier|final
name|String
name|matchField
decl_stmt|;
DECL|field|grok
specifier|private
specifier|final
name|Grok
name|grok
decl_stmt|;
DECL|field|traceMatch
specifier|private
specifier|final
name|boolean
name|traceMatch
decl_stmt|;
DECL|method|GrokProcessor
specifier|public
name|GrokProcessor
parameter_list|(
name|String
name|tag
parameter_list|,
name|Map
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
name|patternBank
parameter_list|,
name|List
argument_list|<
name|String
argument_list|>
name|matchPatterns
parameter_list|,
name|String
name|matchField
parameter_list|)
block|{
name|this
argument_list|(
name|tag
argument_list|,
name|patternBank
argument_list|,
name|matchPatterns
argument_list|,
name|matchField
argument_list|,
literal|false
argument_list|)
expr_stmt|;
block|}
DECL|method|GrokProcessor
specifier|public
name|GrokProcessor
parameter_list|(
name|String
name|tag
parameter_list|,
name|Map
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
name|patternBank
parameter_list|,
name|List
argument_list|<
name|String
argument_list|>
name|matchPatterns
parameter_list|,
name|String
name|matchField
parameter_list|,
name|boolean
name|traceMatch
parameter_list|)
block|{
name|super
argument_list|(
name|tag
argument_list|)
expr_stmt|;
name|this
operator|.
name|matchField
operator|=
name|matchField
expr_stmt|;
name|this
operator|.
name|grok
operator|=
operator|new
name|Grok
argument_list|(
name|patternBank
argument_list|,
name|combinePatterns
argument_list|(
name|matchPatterns
argument_list|,
name|traceMatch
argument_list|)
argument_list|)
expr_stmt|;
name|this
operator|.
name|traceMatch
operator|=
name|traceMatch
expr_stmt|;
block|}
annotation|@
name|Override
DECL|method|execute
specifier|public
name|void
name|execute
parameter_list|(
name|IngestDocument
name|ingestDocument
parameter_list|)
throws|throws
name|Exception
block|{
name|String
name|fieldValue
init|=
name|ingestDocument
operator|.
name|getFieldValue
argument_list|(
name|matchField
argument_list|,
name|String
operator|.
name|class
argument_list|)
decl_stmt|;
name|Map
argument_list|<
name|String
argument_list|,
name|Object
argument_list|>
name|matches
init|=
name|grok
operator|.
name|captures
argument_list|(
name|fieldValue
argument_list|)
decl_stmt|;
if|if
condition|(
name|matches
operator|==
literal|null
condition|)
block|{
throw|throw
operator|new
name|IllegalArgumentException
argument_list|(
literal|"Provided Grok expressions do not match field value: ["
operator|+
name|fieldValue
operator|+
literal|"]"
argument_list|)
throw|;
block|}
name|matches
operator|.
name|entrySet
argument_list|()
operator|.
name|stream
argument_list|()
operator|.
name|filter
argument_list|(
parameter_list|(
name|e
parameter_list|)
lambda|->
name|Objects
operator|.
name|nonNull
argument_list|(
name|e
operator|.
name|getValue
argument_list|()
argument_list|)
argument_list|)
operator|.
name|forEach
argument_list|(
parameter_list|(
name|e
parameter_list|)
lambda|->
name|ingestDocument
operator|.
name|setFieldValue
argument_list|(
name|e
operator|.
name|getKey
argument_list|()
argument_list|,
name|e
operator|.
name|getValue
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
if|if
condition|(
name|traceMatch
condition|)
block|{
annotation|@
name|SuppressWarnings
argument_list|(
literal|"unchecked"
argument_list|)
name|HashMap
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
name|matchMap
init|=
operator|(
name|HashMap
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
operator|)
name|ingestDocument
operator|.
name|getFieldValue
argument_list|(
name|PATTERN_MATCH_KEY
argument_list|,
name|Object
operator|.
name|class
argument_list|)
decl_stmt|;
name|matchMap
operator|.
name|keySet
argument_list|()
operator|.
name|stream
argument_list|()
operator|.
name|findFirst
argument_list|()
operator|.
name|ifPresent
argument_list|(
parameter_list|(
name|index
parameter_list|)
lambda|->
block|{
name|ingestDocument
operator|.
name|setFieldValue
argument_list|(
name|PATTERN_MATCH_KEY
argument_list|,
name|index
argument_list|)
expr_stmt|;
block|}
argument_list|)
expr_stmt|;
block|}
block|}
annotation|@
name|Override
DECL|method|getType
specifier|public
name|String
name|getType
parameter_list|()
block|{
return|return
name|TYPE
return|;
block|}
DECL|method|getGrok
specifier|public
name|Grok
name|getGrok
parameter_list|()
block|{
return|return
name|grok
return|;
block|}
DECL|method|getMatchField
name|String
name|getMatchField
parameter_list|()
block|{
return|return
name|matchField
return|;
block|}
DECL|method|combinePatterns
specifier|static
name|String
name|combinePatterns
parameter_list|(
name|List
argument_list|<
name|String
argument_list|>
name|patterns
parameter_list|,
name|boolean
name|traceMatch
parameter_list|)
block|{
name|String
name|combinedPattern
decl_stmt|;
if|if
condition|(
name|patterns
operator|.
name|size
argument_list|()
operator|>
literal|1
condition|)
block|{
if|if
condition|(
name|traceMatch
condition|)
block|{
name|combinedPattern
operator|=
literal|""
expr_stmt|;
for|for
control|(
name|int
name|i
init|=
literal|0
init|;
name|i
operator|<
name|patterns
operator|.
name|size
argument_list|()
condition|;
name|i
operator|++
control|)
block|{
name|String
name|valueWrap
init|=
literal|"(?<"
operator|+
name|PATTERN_MATCH_KEY
operator|+
literal|"."
operator|+
name|i
operator|+
literal|">"
operator|+
name|patterns
operator|.
name|get
argument_list|(
name|i
argument_list|)
operator|+
literal|")"
decl_stmt|;
if|if
condition|(
name|combinedPattern
operator|.
name|equals
argument_list|(
literal|""
argument_list|)
condition|)
block|{
name|combinedPattern
operator|=
name|valueWrap
expr_stmt|;
block|}
else|else
block|{
name|combinedPattern
operator|=
name|combinedPattern
operator|+
literal|"|"
operator|+
name|valueWrap
expr_stmt|;
block|}
block|}
block|}
else|else
block|{
name|combinedPattern
operator|=
name|patterns
operator|.
name|stream
argument_list|()
operator|.
name|reduce
argument_list|(
literal|""
argument_list|,
parameter_list|(
name|prefix
parameter_list|,
name|value
parameter_list|)
lambda|->
block|{
if|if
condition|(
name|prefix
operator|.
name|equals
argument_list|(
literal|""
argument_list|)
condition|)
block|{
return|return
literal|"(?:"
operator|+
name|value
operator|+
literal|")"
return|;
block|}
else|else
block|{
return|return
name|prefix
operator|+
literal|"|"
operator|+
literal|"(?:"
operator|+
name|value
operator|+
literal|")"
return|;
block|}
block|}
argument_list|)
expr_stmt|;
block|}
block|}
else|else
block|{
name|combinedPattern
operator|=
name|patterns
operator|.
name|get
argument_list|(
literal|0
argument_list|)
expr_stmt|;
block|}
return|return
name|combinedPattern
return|;
block|}
DECL|class|Factory
specifier|public
specifier|final
specifier|static
class|class
name|Factory
implements|implements
name|Processor
operator|.
name|Factory
block|{
DECL|field|builtinPatterns
specifier|private
specifier|final
name|Map
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
name|builtinPatterns
decl_stmt|;
DECL|method|Factory
specifier|public
name|Factory
parameter_list|(
name|Map
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
name|builtinPatterns
parameter_list|)
block|{
name|this
operator|.
name|builtinPatterns
operator|=
name|builtinPatterns
expr_stmt|;
block|}
annotation|@
name|Override
DECL|method|create
specifier|public
name|GrokProcessor
name|create
parameter_list|(
name|Map
argument_list|<
name|String
argument_list|,
name|Processor
operator|.
name|Factory
argument_list|>
name|registry
parameter_list|,
name|String
name|processorTag
parameter_list|,
name|Map
argument_list|<
name|String
argument_list|,
name|Object
argument_list|>
name|config
parameter_list|)
throws|throws
name|Exception
block|{
name|String
name|matchField
init|=
name|ConfigurationUtils
operator|.
name|readStringProperty
argument_list|(
name|TYPE
argument_list|,
name|processorTag
argument_list|,
name|config
argument_list|,
literal|"field"
argument_list|)
decl_stmt|;
name|List
argument_list|<
name|String
argument_list|>
name|matchPatterns
init|=
name|ConfigurationUtils
operator|.
name|readList
argument_list|(
name|TYPE
argument_list|,
name|processorTag
argument_list|,
name|config
argument_list|,
literal|"patterns"
argument_list|)
decl_stmt|;
name|boolean
name|traceMatch
init|=
name|ConfigurationUtils
operator|.
name|readBooleanProperty
argument_list|(
name|TYPE
argument_list|,
name|processorTag
argument_list|,
name|config
argument_list|,
literal|"trace_match"
argument_list|,
literal|false
argument_list|)
decl_stmt|;
if|if
condition|(
name|matchPatterns
operator|.
name|isEmpty
argument_list|()
condition|)
block|{
throw|throw
name|newConfigurationException
argument_list|(
name|TYPE
argument_list|,
name|processorTag
argument_list|,
literal|"patterns"
argument_list|,
literal|"List of patterns must not be empty"
argument_list|)
throw|;
block|}
name|Map
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
name|customPatternBank
init|=
name|ConfigurationUtils
operator|.
name|readOptionalMap
argument_list|(
name|TYPE
argument_list|,
name|processorTag
argument_list|,
name|config
argument_list|,
literal|"pattern_definitions"
argument_list|)
decl_stmt|;
name|Map
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
name|patternBank
init|=
operator|new
name|HashMap
argument_list|<>
argument_list|(
name|builtinPatterns
argument_list|)
decl_stmt|;
if|if
condition|(
name|customPatternBank
operator|!=
literal|null
condition|)
block|{
name|patternBank
operator|.
name|putAll
argument_list|(
name|customPatternBank
argument_list|)
expr_stmt|;
block|}
try|try
block|{
return|return
operator|new
name|GrokProcessor
argument_list|(
name|processorTag
argument_list|,
name|patternBank
argument_list|,
name|matchPatterns
argument_list|,
name|matchField
argument_list|,
name|traceMatch
argument_list|)
return|;
block|}
catch|catch
parameter_list|(
name|Exception
name|e
parameter_list|)
block|{
throw|throw
name|newConfigurationException
argument_list|(
name|TYPE
argument_list|,
name|processorTag
argument_list|,
literal|"patterns"
argument_list|,
literal|"Invalid regex pattern found in: "
operator|+
name|matchPatterns
operator|+
literal|". "
operator|+
name|e
operator|.
name|getMessage
argument_list|()
argument_list|)
throw|;
block|}
block|}
block|}
block|}
end_class

end_unit

