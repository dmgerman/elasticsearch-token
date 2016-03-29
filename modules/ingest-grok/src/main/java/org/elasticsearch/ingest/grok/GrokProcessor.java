begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to Elasticsearch under one or more contributor  * license agreements. See the NOTICE file distributed with  * this work for additional information regarding copyright  * ownership. Elasticsearch licenses this file to you under  * the Apache License, Version 2.0 (the "License"); you may  * not use this file except in compliance with the License.  * You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.ingest.grok
package|package
name|org
operator|.
name|elasticsearch
operator|.
name|ingest
operator|.
name|grok
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
name|core
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
name|core
operator|.
name|AbstractProcessorFactory
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
name|core
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
name|core
operator|.
name|IngestDocument
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
name|Map
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
name|core
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
DECL|method|GrokProcessor
specifier|public
name|GrokProcessor
parameter_list|(
name|String
name|tag
parameter_list|,
name|Grok
name|grok
parameter_list|,
name|String
name|matchField
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
name|grok
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
operator|!=
literal|null
condition|)
block|{
name|matches
operator|.
name|forEach
argument_list|(
parameter_list|(
name|k
parameter_list|,
name|v
parameter_list|)
lambda|->
name|ingestDocument
operator|.
name|setFieldValue
argument_list|(
name|k
argument_list|,
name|v
argument_list|)
argument_list|)
expr_stmt|;
block|}
else|else
block|{
throw|throw
operator|new
name|IllegalArgumentException
argument_list|(
literal|"Grok expression does not match field value: ["
operator|+
name|fieldValue
operator|+
literal|"]"
argument_list|)
throw|;
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
DECL|method|getMatchField
name|String
name|getMatchField
parameter_list|()
block|{
return|return
name|matchField
return|;
block|}
DECL|method|getGrok
name|Grok
name|getGrok
parameter_list|()
block|{
return|return
name|grok
return|;
block|}
DECL|class|Factory
specifier|public
specifier|final
specifier|static
class|class
name|Factory
extends|extends
name|AbstractProcessorFactory
argument_list|<
name|GrokProcessor
argument_list|>
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
DECL|method|doCreate
specifier|public
name|GrokProcessor
name|doCreate
parameter_list|(
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
name|String
name|matchPattern
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
literal|"pattern"
argument_list|)
decl_stmt|;
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
name|Grok
name|grok
decl_stmt|;
try|try
block|{
name|grok
operator|=
operator|new
name|Grok
argument_list|(
name|patternBank
argument_list|,
name|matchPattern
argument_list|)
expr_stmt|;
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
literal|"pattern"
argument_list|,
literal|"Invalid regex pattern. "
operator|+
name|e
operator|.
name|getMessage
argument_list|()
argument_list|)
throw|;
block|}
return|return
operator|new
name|GrokProcessor
argument_list|(
name|processorTag
argument_list|,
name|grok
argument_list|,
name|matchField
argument_list|)
return|;
block|}
block|}
block|}
end_class

end_unit

