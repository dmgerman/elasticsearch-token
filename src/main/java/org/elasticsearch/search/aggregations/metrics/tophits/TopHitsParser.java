begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to Elasticsearch under one or more contributor  * license agreements. See the NOTICE file distributed with  * this work for additional information regarding copyright  * ownership. Elasticsearch licenses this file to you under  * the Apache License, Version 2.0 (the "License"); you may  * not use this file except in compliance with the License.  * You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.search.aggregations.metrics.tophits
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
name|tophits
package|;
end_package

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|ExceptionsHelper
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
name|inject
operator|.
name|Inject
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
name|search
operator|.
name|SearchParseException
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
name|AggregatorFactory
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
name|fetch
operator|.
name|FetchPhase
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
name|fetch
operator|.
name|fielddata
operator|.
name|FieldDataFieldsParseElement
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
name|fetch
operator|.
name|script
operator|.
name|ScriptFieldsParseElement
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
name|fetch
operator|.
name|source
operator|.
name|FetchSourceParseElement
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
name|highlight
operator|.
name|HighlighterParseElement
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
name|internal
operator|.
name|SearchContext
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
name|internal
operator|.
name|SubSearchContext
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
name|sort
operator|.
name|SortParseElement
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

begin_comment
comment|/**  *  */
end_comment

begin_class
DECL|class|TopHitsParser
specifier|public
class|class
name|TopHitsParser
implements|implements
name|Aggregator
operator|.
name|Parser
block|{
DECL|field|fetchPhase
specifier|private
specifier|final
name|FetchPhase
name|fetchPhase
decl_stmt|;
DECL|field|sortParseElement
specifier|private
specifier|final
name|SortParseElement
name|sortParseElement
decl_stmt|;
DECL|field|sourceParseElement
specifier|private
specifier|final
name|FetchSourceParseElement
name|sourceParseElement
decl_stmt|;
DECL|field|highlighterParseElement
specifier|private
specifier|final
name|HighlighterParseElement
name|highlighterParseElement
decl_stmt|;
DECL|field|fieldDataFieldsParseElement
specifier|private
specifier|final
name|FieldDataFieldsParseElement
name|fieldDataFieldsParseElement
decl_stmt|;
DECL|field|scriptFieldsParseElement
specifier|private
specifier|final
name|ScriptFieldsParseElement
name|scriptFieldsParseElement
decl_stmt|;
annotation|@
name|Inject
DECL|method|TopHitsParser
specifier|public
name|TopHitsParser
parameter_list|(
name|FetchPhase
name|fetchPhase
parameter_list|,
name|SortParseElement
name|sortParseElement
parameter_list|,
name|FetchSourceParseElement
name|sourceParseElement
parameter_list|,
name|HighlighterParseElement
name|highlighterParseElement
parameter_list|,
name|FieldDataFieldsParseElement
name|fieldDataFieldsParseElement
parameter_list|,
name|ScriptFieldsParseElement
name|scriptFieldsParseElement
parameter_list|)
block|{
name|this
operator|.
name|fetchPhase
operator|=
name|fetchPhase
expr_stmt|;
name|this
operator|.
name|sortParseElement
operator|=
name|sortParseElement
expr_stmt|;
name|this
operator|.
name|sourceParseElement
operator|=
name|sourceParseElement
expr_stmt|;
name|this
operator|.
name|highlighterParseElement
operator|=
name|highlighterParseElement
expr_stmt|;
name|this
operator|.
name|fieldDataFieldsParseElement
operator|=
name|fieldDataFieldsParseElement
expr_stmt|;
name|this
operator|.
name|scriptFieldsParseElement
operator|=
name|scriptFieldsParseElement
expr_stmt|;
block|}
annotation|@
name|Override
DECL|method|type
specifier|public
name|String
name|type
parameter_list|()
block|{
return|return
name|InternalTopHits
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
name|AggregatorFactory
name|parse
parameter_list|(
name|String
name|aggregationName
parameter_list|,
name|XContentParser
name|parser
parameter_list|,
name|SearchContext
name|context
parameter_list|)
throws|throws
name|IOException
block|{
name|SubSearchContext
name|subSearchContext
init|=
operator|new
name|SubSearchContext
argument_list|(
name|context
argument_list|)
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
try|try
block|{
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
literal|"sort"
operator|.
name|equals
argument_list|(
name|currentFieldName
argument_list|)
condition|)
block|{
name|sortParseElement
operator|.
name|parse
argument_list|(
name|parser
argument_list|,
name|subSearchContext
argument_list|)
expr_stmt|;
block|}
elseif|else
if|if
condition|(
literal|"_source"
operator|.
name|equals
argument_list|(
name|currentFieldName
argument_list|)
condition|)
block|{
name|sourceParseElement
operator|.
name|parse
argument_list|(
name|parser
argument_list|,
name|subSearchContext
argument_list|)
expr_stmt|;
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
switch|switch
condition|(
name|currentFieldName
condition|)
block|{
case|case
literal|"from"
case|:
name|subSearchContext
operator|.
name|from
argument_list|(
name|parser
operator|.
name|intValue
argument_list|()
argument_list|)
expr_stmt|;
break|break;
case|case
literal|"size"
case|:
name|subSearchContext
operator|.
name|size
argument_list|(
name|parser
operator|.
name|intValue
argument_list|()
argument_list|)
expr_stmt|;
break|break;
case|case
literal|"track_scores"
case|:
case|case
literal|"trackScores"
case|:
name|subSearchContext
operator|.
name|trackScores
argument_list|(
name|parser
operator|.
name|booleanValue
argument_list|()
argument_list|)
expr_stmt|;
break|break;
case|case
literal|"version"
case|:
name|subSearchContext
operator|.
name|version
argument_list|(
name|parser
operator|.
name|booleanValue
argument_list|()
argument_list|)
expr_stmt|;
break|break;
case|case
literal|"explain"
case|:
name|subSearchContext
operator|.
name|explain
argument_list|(
name|parser
operator|.
name|booleanValue
argument_list|()
argument_list|)
expr_stmt|;
break|break;
default|default:
throw|throw
operator|new
name|SearchParseException
argument_list|(
name|context
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
operator|==
name|XContentParser
operator|.
name|Token
operator|.
name|START_OBJECT
condition|)
block|{
switch|switch
condition|(
name|currentFieldName
condition|)
block|{
case|case
literal|"highlight"
case|:
name|highlighterParseElement
operator|.
name|parse
argument_list|(
name|parser
argument_list|,
name|subSearchContext
argument_list|)
expr_stmt|;
break|break;
case|case
literal|"scriptFields"
case|:
case|case
literal|"script_fields"
case|:
name|scriptFieldsParseElement
operator|.
name|parse
argument_list|(
name|parser
argument_list|,
name|subSearchContext
argument_list|)
expr_stmt|;
break|break;
default|default:
throw|throw
operator|new
name|SearchParseException
argument_list|(
name|context
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
operator|==
name|XContentParser
operator|.
name|Token
operator|.
name|START_ARRAY
condition|)
block|{
switch|switch
condition|(
name|currentFieldName
condition|)
block|{
case|case
literal|"fielddataFields"
case|:
case|case
literal|"fielddata_fields"
case|:
name|fieldDataFieldsParseElement
operator|.
name|parse
argument_list|(
name|parser
argument_list|,
name|subSearchContext
argument_list|)
expr_stmt|;
break|break;
default|default:
throw|throw
operator|new
name|SearchParseException
argument_list|(
name|context
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
name|SearchParseException
argument_list|(
name|context
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
block|}
catch|catch
parameter_list|(
name|Exception
name|e
parameter_list|)
block|{
throw|throw
name|ExceptionsHelper
operator|.
name|convertToElastic
argument_list|(
name|e
argument_list|)
throw|;
block|}
return|return
operator|new
name|TopHitsAggregator
operator|.
name|Factory
argument_list|(
name|aggregationName
argument_list|,
name|fetchPhase
argument_list|,
name|subSearchContext
argument_list|)
return|;
block|}
block|}
end_class

end_unit

