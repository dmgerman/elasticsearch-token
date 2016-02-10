begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to Elasticsearch under one or more contributor  * license agreements. See the NOTICE file distributed with  * this work for additional information regarding copyright  * ownership. Elasticsearch licenses this file to you under  * the Apache License, Version 2.0 (the "License"); you may  * not use this file except in compliance with the License.  * You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.search.aggregations.metrics.scripted
package|package
name|org
operator|.
name|elasticsearch
operator|.
name|search
operator|.
name|aggregations
operator|.
name|metrics
operator|.
name|scripted
package|;
end_package

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|common
operator|.
name|ParseField
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|common
operator|.
name|ParsingException
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|common
operator|.
name|xcontent
operator|.
name|XContentParser
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|index
operator|.
name|query
operator|.
name|QueryParseContext
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|script
operator|.
name|Script
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|script
operator|.
name|ScriptParameterParser
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|script
operator|.
name|ScriptParameterParser
operator|.
name|ScriptParameterValue
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|search
operator|.
name|aggregations
operator|.
name|Aggregator
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|search
operator|.
name|aggregations
operator|.
name|AggregatorBuilder
import|;
end_import

begin_import
import|import
name|java
operator|.
name|io
operator|.
name|IOException
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|HashSet
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
name|Set
import|;
end_import

begin_class
DECL|class|ScriptedMetricParser
specifier|public
class|class
name|ScriptedMetricParser
implements|implements
name|Aggregator
operator|.
name|Parser
block|{
DECL|field|INIT_SCRIPT
specifier|public
specifier|static
specifier|final
name|String
name|INIT_SCRIPT
init|=
literal|"init_script"
decl_stmt|;
DECL|field|MAP_SCRIPT
specifier|public
specifier|static
specifier|final
name|String
name|MAP_SCRIPT
init|=
literal|"map_script"
decl_stmt|;
DECL|field|COMBINE_SCRIPT
specifier|public
specifier|static
specifier|final
name|String
name|COMBINE_SCRIPT
init|=
literal|"combine_script"
decl_stmt|;
DECL|field|REDUCE_SCRIPT
specifier|public
specifier|static
specifier|final
name|String
name|REDUCE_SCRIPT
init|=
literal|"reduce_script"
decl_stmt|;
DECL|field|INIT_SCRIPT_FIELD
specifier|public
specifier|static
specifier|final
name|ParseField
name|INIT_SCRIPT_FIELD
init|=
operator|new
name|ParseField
argument_list|(
literal|"init_script"
argument_list|)
decl_stmt|;
DECL|field|MAP_SCRIPT_FIELD
specifier|public
specifier|static
specifier|final
name|ParseField
name|MAP_SCRIPT_FIELD
init|=
operator|new
name|ParseField
argument_list|(
literal|"map_script"
argument_list|)
decl_stmt|;
DECL|field|COMBINE_SCRIPT_FIELD
specifier|public
specifier|static
specifier|final
name|ParseField
name|COMBINE_SCRIPT_FIELD
init|=
operator|new
name|ParseField
argument_list|(
literal|"combine_script"
argument_list|)
decl_stmt|;
DECL|field|REDUCE_SCRIPT_FIELD
specifier|public
specifier|static
specifier|final
name|ParseField
name|REDUCE_SCRIPT_FIELD
init|=
operator|new
name|ParseField
argument_list|(
literal|"reduce_script"
argument_list|)
decl_stmt|;
DECL|field|PARAMS_FIELD
specifier|public
specifier|static
specifier|final
name|ParseField
name|PARAMS_FIELD
init|=
operator|new
name|ParseField
argument_list|(
literal|"params"
argument_list|)
decl_stmt|;
DECL|field|REDUCE_PARAMS_FIELD
specifier|public
specifier|static
specifier|final
name|ParseField
name|REDUCE_PARAMS_FIELD
init|=
operator|new
name|ParseField
argument_list|(
literal|"reduce_params"
argument_list|)
decl_stmt|;
DECL|field|LANG_FIELD
specifier|public
specifier|static
specifier|final
name|ParseField
name|LANG_FIELD
init|=
operator|new
name|ParseField
argument_list|(
literal|"lang"
argument_list|)
decl_stmt|;
annotation|@
name|Override
DECL|method|type
specifier|public
name|String
name|type
parameter_list|()
block|{
return|return
name|InternalScriptedMetric
operator|.
name|TYPE
operator|.
name|name
argument_list|()
return|;
block|}
annotation|@
name|Override
DECL|method|parse
specifier|public
name|AggregatorBuilder
name|parse
parameter_list|(
name|String
name|aggregationName
parameter_list|,
name|XContentParser
name|parser
parameter_list|,
name|QueryParseContext
name|context
parameter_list|)
throws|throws
name|IOException
block|{
name|Script
name|initScript
init|=
literal|null
decl_stmt|;
name|Script
name|mapScript
init|=
literal|null
decl_stmt|;
name|Script
name|combineScript
init|=
literal|null
decl_stmt|;
name|Script
name|reduceScript
init|=
literal|null
decl_stmt|;
name|Map
argument_list|<
name|String
argument_list|,
name|Object
argument_list|>
name|params
init|=
literal|null
decl_stmt|;
name|Map
argument_list|<
name|String
argument_list|,
name|Object
argument_list|>
name|reduceParams
init|=
literal|null
decl_stmt|;
name|XContentParser
operator|.
name|Token
name|token
decl_stmt|;
name|String
name|currentFieldName
init|=
literal|null
decl_stmt|;
name|Set
argument_list|<
name|String
argument_list|>
name|scriptParameters
init|=
operator|new
name|HashSet
argument_list|<>
argument_list|()
decl_stmt|;
name|scriptParameters
operator|.
name|add
argument_list|(
name|INIT_SCRIPT
argument_list|)
expr_stmt|;
name|scriptParameters
operator|.
name|add
argument_list|(
name|MAP_SCRIPT
argument_list|)
expr_stmt|;
name|scriptParameters
operator|.
name|add
argument_list|(
name|COMBINE_SCRIPT
argument_list|)
expr_stmt|;
name|scriptParameters
operator|.
name|add
argument_list|(
name|REDUCE_SCRIPT
argument_list|)
expr_stmt|;
name|ScriptParameterParser
name|scriptParameterParser
init|=
operator|new
name|ScriptParameterParser
argument_list|(
name|scriptParameters
argument_list|)
decl_stmt|;
while|while
condition|(
operator|(
name|token
operator|=
name|parser
operator|.
name|nextToken
argument_list|()
operator|)
operator|!=
name|XContentParser
operator|.
name|Token
operator|.
name|END_OBJECT
condition|)
block|{
if|if
condition|(
name|token
operator|==
name|XContentParser
operator|.
name|Token
operator|.
name|FIELD_NAME
condition|)
block|{
name|currentFieldName
operator|=
name|parser
operator|.
name|currentName
argument_list|()
expr_stmt|;
block|}
elseif|else
if|if
condition|(
name|token
operator|==
name|XContentParser
operator|.
name|Token
operator|.
name|START_OBJECT
condition|)
block|{
if|if
condition|(
name|context
operator|.
name|parseFieldMatcher
argument_list|()
operator|.
name|match
argument_list|(
name|currentFieldName
argument_list|,
name|INIT_SCRIPT_FIELD
argument_list|)
condition|)
block|{
name|initScript
operator|=
name|Script
operator|.
name|parse
argument_list|(
name|parser
argument_list|,
name|context
operator|.
name|parseFieldMatcher
argument_list|()
argument_list|)
expr_stmt|;
block|}
elseif|else
if|if
condition|(
name|context
operator|.
name|parseFieldMatcher
argument_list|()
operator|.
name|match
argument_list|(
name|currentFieldName
argument_list|,
name|MAP_SCRIPT_FIELD
argument_list|)
condition|)
block|{
name|mapScript
operator|=
name|Script
operator|.
name|parse
argument_list|(
name|parser
argument_list|,
name|context
operator|.
name|parseFieldMatcher
argument_list|()
argument_list|)
expr_stmt|;
block|}
elseif|else
if|if
condition|(
name|context
operator|.
name|parseFieldMatcher
argument_list|()
operator|.
name|match
argument_list|(
name|currentFieldName
argument_list|,
name|COMBINE_SCRIPT_FIELD
argument_list|)
condition|)
block|{
name|combineScript
operator|=
name|Script
operator|.
name|parse
argument_list|(
name|parser
argument_list|,
name|context
operator|.
name|parseFieldMatcher
argument_list|()
argument_list|)
expr_stmt|;
block|}
elseif|else
if|if
condition|(
name|context
operator|.
name|parseFieldMatcher
argument_list|()
operator|.
name|match
argument_list|(
name|currentFieldName
argument_list|,
name|REDUCE_SCRIPT_FIELD
argument_list|)
condition|)
block|{
name|reduceScript
operator|=
name|Script
operator|.
name|parse
argument_list|(
name|parser
argument_list|,
name|context
operator|.
name|parseFieldMatcher
argument_list|()
argument_list|)
expr_stmt|;
block|}
elseif|else
if|if
condition|(
name|context
operator|.
name|parseFieldMatcher
argument_list|()
operator|.
name|match
argument_list|(
name|currentFieldName
argument_list|,
name|PARAMS_FIELD
argument_list|)
condition|)
block|{
name|params
operator|=
name|parser
operator|.
name|map
argument_list|()
expr_stmt|;
block|}
elseif|else
if|if
condition|(
name|context
operator|.
name|parseFieldMatcher
argument_list|()
operator|.
name|match
argument_list|(
name|currentFieldName
argument_list|,
name|REDUCE_PARAMS_FIELD
argument_list|)
condition|)
block|{
name|reduceParams
operator|=
name|parser
operator|.
name|map
argument_list|()
expr_stmt|;
block|}
else|else
block|{
throw|throw
operator|new
name|ParsingException
argument_list|(
name|parser
operator|.
name|getTokenLocation
argument_list|()
argument_list|,
literal|"Unknown key for a "
operator|+
name|token
operator|+
literal|" in ["
operator|+
name|aggregationName
operator|+
literal|"]: ["
operator|+
name|currentFieldName
operator|+
literal|"]."
argument_list|)
throw|;
block|}
block|}
elseif|else
if|if
condition|(
name|token
operator|.
name|isValue
argument_list|()
condition|)
block|{
if|if
condition|(
operator|!
name|scriptParameterParser
operator|.
name|token
argument_list|(
name|currentFieldName
argument_list|,
name|token
argument_list|,
name|parser
argument_list|,
name|context
operator|.
name|parseFieldMatcher
argument_list|()
argument_list|)
condition|)
block|{
throw|throw
operator|new
name|ParsingException
argument_list|(
name|parser
operator|.
name|getTokenLocation
argument_list|()
argument_list|,
literal|"Unknown key for a "
operator|+
name|token
operator|+
literal|" in ["
operator|+
name|aggregationName
operator|+
literal|"]: ["
operator|+
name|currentFieldName
operator|+
literal|"]."
argument_list|)
throw|;
block|}
block|}
else|else
block|{
throw|throw
operator|new
name|ParsingException
argument_list|(
name|parser
operator|.
name|getTokenLocation
argument_list|()
argument_list|,
literal|"Unexpected token "
operator|+
name|token
operator|+
literal|" in ["
operator|+
name|aggregationName
operator|+
literal|"]."
argument_list|)
throw|;
block|}
block|}
if|if
condition|(
name|initScript
operator|==
literal|null
condition|)
block|{
comment|// Didn't find anything using the new API so try using the old one instead
name|ScriptParameterValue
name|scriptValue
init|=
name|scriptParameterParser
operator|.
name|getScriptParameterValue
argument_list|(
name|INIT_SCRIPT
argument_list|)
decl_stmt|;
if|if
condition|(
name|scriptValue
operator|!=
literal|null
condition|)
block|{
name|initScript
operator|=
operator|new
name|Script
argument_list|(
name|scriptValue
operator|.
name|script
argument_list|()
argument_list|,
name|scriptValue
operator|.
name|scriptType
argument_list|()
argument_list|,
name|scriptParameterParser
operator|.
name|lang
argument_list|()
argument_list|,
name|params
argument_list|)
expr_stmt|;
block|}
block|}
elseif|else
if|if
condition|(
name|initScript
operator|.
name|getParams
argument_list|()
operator|!=
literal|null
condition|)
block|{
throw|throw
operator|new
name|ParsingException
argument_list|(
name|parser
operator|.
name|getTokenLocation
argument_list|()
argument_list|,
literal|"init_script params are not supported. Parameters for the init_script must be specified in the params field on the scripted_metric aggregator not inside the init_script object"
argument_list|)
throw|;
block|}
if|if
condition|(
name|mapScript
operator|==
literal|null
condition|)
block|{
comment|// Didn't find anything using the new API so try using the old one instead
name|ScriptParameterValue
name|scriptValue
init|=
name|scriptParameterParser
operator|.
name|getScriptParameterValue
argument_list|(
name|MAP_SCRIPT
argument_list|)
decl_stmt|;
if|if
condition|(
name|scriptValue
operator|!=
literal|null
condition|)
block|{
name|mapScript
operator|=
operator|new
name|Script
argument_list|(
name|scriptValue
operator|.
name|script
argument_list|()
argument_list|,
name|scriptValue
operator|.
name|scriptType
argument_list|()
argument_list|,
name|scriptParameterParser
operator|.
name|lang
argument_list|()
argument_list|,
name|params
argument_list|)
expr_stmt|;
block|}
block|}
elseif|else
if|if
condition|(
name|mapScript
operator|.
name|getParams
argument_list|()
operator|!=
literal|null
condition|)
block|{
throw|throw
operator|new
name|ParsingException
argument_list|(
name|parser
operator|.
name|getTokenLocation
argument_list|()
argument_list|,
literal|"map_script params are not supported. Parameters for the map_script must be specified in the params field on the scripted_metric aggregator not inside the map_script object"
argument_list|)
throw|;
block|}
if|if
condition|(
name|combineScript
operator|==
literal|null
condition|)
block|{
comment|// Didn't find anything using the new API so try using the old one instead
name|ScriptParameterValue
name|scriptValue
init|=
name|scriptParameterParser
operator|.
name|getScriptParameterValue
argument_list|(
name|COMBINE_SCRIPT
argument_list|)
decl_stmt|;
if|if
condition|(
name|scriptValue
operator|!=
literal|null
condition|)
block|{
name|combineScript
operator|=
operator|new
name|Script
argument_list|(
name|scriptValue
operator|.
name|script
argument_list|()
argument_list|,
name|scriptValue
operator|.
name|scriptType
argument_list|()
argument_list|,
name|scriptParameterParser
operator|.
name|lang
argument_list|()
argument_list|,
name|params
argument_list|)
expr_stmt|;
block|}
block|}
elseif|else
if|if
condition|(
name|combineScript
operator|.
name|getParams
argument_list|()
operator|!=
literal|null
condition|)
block|{
throw|throw
operator|new
name|ParsingException
argument_list|(
name|parser
operator|.
name|getTokenLocation
argument_list|()
argument_list|,
literal|"combine_script params are not supported. Parameters for the combine_script must be specified in the params field on the scripted_metric aggregator not inside the combine_script object"
argument_list|)
throw|;
block|}
if|if
condition|(
name|reduceScript
operator|==
literal|null
condition|)
block|{
comment|// Didn't find anything using the new API so try using the old one instead
name|ScriptParameterValue
name|scriptValue
init|=
name|scriptParameterParser
operator|.
name|getScriptParameterValue
argument_list|(
name|REDUCE_SCRIPT
argument_list|)
decl_stmt|;
if|if
condition|(
name|scriptValue
operator|!=
literal|null
condition|)
block|{
name|reduceScript
operator|=
operator|new
name|Script
argument_list|(
name|scriptValue
operator|.
name|script
argument_list|()
argument_list|,
name|scriptValue
operator|.
name|scriptType
argument_list|()
argument_list|,
name|scriptParameterParser
operator|.
name|lang
argument_list|()
argument_list|,
name|reduceParams
argument_list|)
expr_stmt|;
block|}
block|}
if|if
condition|(
name|mapScript
operator|==
literal|null
condition|)
block|{
throw|throw
operator|new
name|ParsingException
argument_list|(
name|parser
operator|.
name|getTokenLocation
argument_list|()
argument_list|,
literal|"map_script field is required in ["
operator|+
name|aggregationName
operator|+
literal|"]."
argument_list|)
throw|;
block|}
name|ScriptedMetricAggregator
operator|.
name|ScriptedMetricAggregatorBuilder
name|factory
init|=
operator|new
name|ScriptedMetricAggregator
operator|.
name|ScriptedMetricAggregatorBuilder
argument_list|(
name|aggregationName
argument_list|)
decl_stmt|;
if|if
condition|(
name|initScript
operator|!=
literal|null
condition|)
block|{
name|factory
operator|.
name|initScript
argument_list|(
name|initScript
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|mapScript
operator|!=
literal|null
condition|)
block|{
name|factory
operator|.
name|mapScript
argument_list|(
name|mapScript
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|combineScript
operator|!=
literal|null
condition|)
block|{
name|factory
operator|.
name|combineScript
argument_list|(
name|combineScript
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|reduceScript
operator|!=
literal|null
condition|)
block|{
name|factory
operator|.
name|reduceScript
argument_list|(
name|reduceScript
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|params
operator|!=
literal|null
condition|)
block|{
name|factory
operator|.
name|params
argument_list|(
name|params
argument_list|)
expr_stmt|;
block|}
return|return
name|factory
return|;
block|}
annotation|@
name|Override
DECL|method|getFactoryPrototypes
specifier|public
name|AggregatorBuilder
argument_list|<
name|?
argument_list|>
name|getFactoryPrototypes
parameter_list|()
block|{
return|return
name|ScriptedMetricAggregator
operator|.
name|ScriptedMetricAggregatorBuilder
operator|.
name|PROTOTYPE
return|;
block|}
block|}
end_class

end_unit

