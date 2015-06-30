begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to Elasticsearch under one or more contributor  * license agreements. See the NOTICE file distributed with  * this work for additional information regarding copyright  * ownership. Elasticsearch licenses this file to you under  * the Apache License, Version 2.0 (the "License"); you may  * not use this file except in compliance with the License.  * You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.index.query
package|package
name|org
operator|.
name|elasticsearch
operator|.
name|index
operator|.
name|query
package|;
end_package

begin_import
import|import
name|com
operator|.
name|google
operator|.
name|common
operator|.
name|base
operator|.
name|Objects
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|lucene
operator|.
name|index
operator|.
name|LeafReaderContext
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|lucene
operator|.
name|search
operator|.
name|IndexSearcher
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|lucene
operator|.
name|search
operator|.
name|Query
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|lucene
operator|.
name|search
operator|.
name|RandomAccessWeight
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|lucene
operator|.
name|search
operator|.
name|Weight
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|lucene
operator|.
name|util
operator|.
name|Bits
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
name|script
operator|.
name|LeafSearchScript
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
name|Script
operator|.
name|ScriptField
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
name|ScriptContext
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
name|script
operator|.
name|ScriptService
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
name|SearchScript
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
name|lookup
operator|.
name|SearchLookup
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
name|Map
import|;
end_import

begin_import
import|import static
name|com
operator|.
name|google
operator|.
name|common
operator|.
name|collect
operator|.
name|Maps
operator|.
name|newHashMap
import|;
end_import

begin_comment
comment|/**  *  */
end_comment

begin_class
DECL|class|ScriptQueryParser
specifier|public
class|class
name|ScriptQueryParser
extends|extends
name|BaseQueryParserTemp
block|{
annotation|@
name|Inject
DECL|method|ScriptQueryParser
specifier|public
name|ScriptQueryParser
parameter_list|()
block|{     }
annotation|@
name|Override
DECL|method|names
specifier|public
name|String
index|[]
name|names
parameter_list|()
block|{
return|return
operator|new
name|String
index|[]
block|{
name|ScriptQueryBuilder
operator|.
name|NAME
block|}
return|;
block|}
annotation|@
name|Override
DECL|method|parse
specifier|public
name|Query
name|parse
parameter_list|(
name|QueryParseContext
name|parseContext
parameter_list|)
throws|throws
name|IOException
throws|,
name|QueryParsingException
block|{
name|XContentParser
name|parser
init|=
name|parseContext
operator|.
name|parser
argument_list|()
decl_stmt|;
name|ScriptParameterParser
name|scriptParameterParser
init|=
operator|new
name|ScriptParameterParser
argument_list|()
decl_stmt|;
name|XContentParser
operator|.
name|Token
name|token
decl_stmt|;
comment|// also, when caching, since its isCacheable is false, will result in loading all bit set...
name|Script
name|script
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
name|float
name|boost
init|=
name|AbstractQueryBuilder
operator|.
name|DEFAULT_BOOST
decl_stmt|;
name|String
name|queryName
init|=
literal|null
decl_stmt|;
name|String
name|currentFieldName
init|=
literal|null
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
name|parseContext
operator|.
name|isDeprecatedSetting
argument_list|(
name|currentFieldName
argument_list|)
condition|)
block|{
comment|// skip
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
name|ScriptField
operator|.
name|SCRIPT
operator|.
name|match
argument_list|(
name|currentFieldName
argument_list|)
condition|)
block|{
name|script
operator|=
name|Script
operator|.
name|parse
argument_list|(
name|parser
argument_list|)
expr_stmt|;
block|}
elseif|else
if|if
condition|(
literal|"params"
operator|.
name|equals
argument_list|(
name|currentFieldName
argument_list|)
condition|)
block|{
comment|// TODO remove in 2.0 (here to support old script APIs)
name|params
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
name|QueryParsingException
argument_list|(
name|parseContext
argument_list|,
literal|"[script] query does not support ["
operator|+
name|currentFieldName
operator|+
literal|"]"
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
literal|"_name"
operator|.
name|equals
argument_list|(
name|currentFieldName
argument_list|)
condition|)
block|{
name|queryName
operator|=
name|parser
operator|.
name|text
argument_list|()
expr_stmt|;
block|}
elseif|else
if|if
condition|(
literal|"boost"
operator|.
name|equals
argument_list|(
name|currentFieldName
argument_list|)
condition|)
block|{
name|boost
operator|=
name|parser
operator|.
name|floatValue
argument_list|()
expr_stmt|;
block|}
elseif|else
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
argument_list|)
condition|)
block|{
throw|throw
operator|new
name|QueryParsingException
argument_list|(
name|parseContext
argument_list|,
literal|"[script] query does not support ["
operator|+
name|currentFieldName
operator|+
literal|"]"
argument_list|)
throw|;
block|}
block|}
block|}
if|if
condition|(
name|script
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
name|getDefaultScriptParameterValue
argument_list|()
decl_stmt|;
if|if
condition|(
name|scriptValue
operator|!=
literal|null
condition|)
block|{
if|if
condition|(
name|params
operator|==
literal|null
condition|)
block|{
name|params
operator|=
name|newHashMap
argument_list|()
expr_stmt|;
block|}
name|script
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
name|params
operator|!=
literal|null
condition|)
block|{
throw|throw
operator|new
name|QueryParsingException
argument_list|(
name|parseContext
argument_list|,
literal|"script params must be specified inside script object in a [script] filter"
argument_list|)
throw|;
block|}
if|if
condition|(
name|script
operator|==
literal|null
condition|)
block|{
throw|throw
operator|new
name|QueryParsingException
argument_list|(
name|parseContext
argument_list|,
literal|"script must be provided with a [script] filter"
argument_list|)
throw|;
block|}
name|Query
name|query
init|=
operator|new
name|ScriptQuery
argument_list|(
name|script
argument_list|,
name|parseContext
operator|.
name|scriptService
argument_list|()
argument_list|,
name|parseContext
operator|.
name|lookup
argument_list|()
argument_list|)
decl_stmt|;
if|if
condition|(
name|queryName
operator|!=
literal|null
condition|)
block|{
name|parseContext
operator|.
name|addNamedQuery
argument_list|(
name|queryName
argument_list|,
name|query
argument_list|)
expr_stmt|;
block|}
name|query
operator|.
name|setBoost
argument_list|(
name|boost
argument_list|)
expr_stmt|;
return|return
name|query
return|;
block|}
DECL|class|ScriptQuery
specifier|static
class|class
name|ScriptQuery
extends|extends
name|Query
block|{
DECL|field|script
specifier|private
specifier|final
name|Script
name|script
decl_stmt|;
DECL|field|searchScript
specifier|private
specifier|final
name|SearchScript
name|searchScript
decl_stmt|;
DECL|method|ScriptQuery
specifier|public
name|ScriptQuery
parameter_list|(
name|Script
name|script
parameter_list|,
name|ScriptService
name|scriptService
parameter_list|,
name|SearchLookup
name|searchLookup
parameter_list|)
block|{
name|this
operator|.
name|script
operator|=
name|script
expr_stmt|;
name|this
operator|.
name|searchScript
operator|=
name|scriptService
operator|.
name|search
argument_list|(
name|searchLookup
argument_list|,
name|script
argument_list|,
name|ScriptContext
operator|.
name|Standard
operator|.
name|SEARCH
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
DECL|method|toString
specifier|public
name|String
name|toString
parameter_list|(
name|String
name|field
parameter_list|)
block|{
name|StringBuilder
name|buffer
init|=
operator|new
name|StringBuilder
argument_list|()
decl_stmt|;
name|buffer
operator|.
name|append
argument_list|(
literal|"ScriptFilter("
argument_list|)
expr_stmt|;
name|buffer
operator|.
name|append
argument_list|(
name|script
argument_list|)
expr_stmt|;
name|buffer
operator|.
name|append
argument_list|(
literal|")"
argument_list|)
expr_stmt|;
return|return
name|buffer
operator|.
name|toString
argument_list|()
return|;
block|}
annotation|@
name|Override
DECL|method|equals
specifier|public
name|boolean
name|equals
parameter_list|(
name|Object
name|obj
parameter_list|)
block|{
if|if
condition|(
name|this
operator|==
name|obj
condition|)
return|return
literal|true
return|;
if|if
condition|(
operator|!
name|super
operator|.
name|equals
argument_list|(
name|obj
argument_list|)
condition|)
return|return
literal|false
return|;
name|ScriptQuery
name|other
init|=
operator|(
name|ScriptQuery
operator|)
name|obj
decl_stmt|;
return|return
name|Objects
operator|.
name|equal
argument_list|(
name|script
argument_list|,
name|other
operator|.
name|script
argument_list|)
return|;
block|}
annotation|@
name|Override
DECL|method|hashCode
specifier|public
name|int
name|hashCode
parameter_list|()
block|{
specifier|final
name|int
name|prime
init|=
literal|31
decl_stmt|;
name|int
name|result
init|=
name|super
operator|.
name|hashCode
argument_list|()
decl_stmt|;
name|result
operator|=
name|prime
operator|*
name|result
operator|+
name|Objects
operator|.
name|hashCode
argument_list|(
name|script
argument_list|)
expr_stmt|;
return|return
name|result
return|;
block|}
annotation|@
name|Override
DECL|method|createWeight
specifier|public
name|Weight
name|createWeight
parameter_list|(
name|IndexSearcher
name|searcher
parameter_list|,
name|boolean
name|needsScores
parameter_list|)
throws|throws
name|IOException
block|{
return|return
operator|new
name|RandomAccessWeight
argument_list|(
name|this
argument_list|)
block|{
annotation|@
name|Override
specifier|protected
name|Bits
name|getMatchingDocs
parameter_list|(
specifier|final
name|LeafReaderContext
name|context
parameter_list|)
throws|throws
name|IOException
block|{
specifier|final
name|LeafSearchScript
name|leafScript
init|=
name|searchScript
operator|.
name|getLeafSearchScript
argument_list|(
name|context
argument_list|)
decl_stmt|;
return|return
operator|new
name|Bits
argument_list|()
block|{
annotation|@
name|Override
specifier|public
name|boolean
name|get
parameter_list|(
name|int
name|doc
parameter_list|)
block|{
name|leafScript
operator|.
name|setDocument
argument_list|(
name|doc
argument_list|)
expr_stmt|;
name|Object
name|val
init|=
name|leafScript
operator|.
name|run
argument_list|()
decl_stmt|;
if|if
condition|(
name|val
operator|==
literal|null
condition|)
block|{
return|return
literal|false
return|;
block|}
if|if
condition|(
name|val
operator|instanceof
name|Boolean
condition|)
block|{
return|return
operator|(
name|Boolean
operator|)
name|val
return|;
block|}
if|if
condition|(
name|val
operator|instanceof
name|Number
condition|)
block|{
return|return
operator|(
operator|(
name|Number
operator|)
name|val
operator|)
operator|.
name|longValue
argument_list|()
operator|!=
literal|0
return|;
block|}
throw|throw
operator|new
name|IllegalArgumentException
argument_list|(
literal|"Can't handle type ["
operator|+
name|val
operator|+
literal|"] in script filter"
argument_list|)
throw|;
block|}
annotation|@
name|Override
specifier|public
name|int
name|length
parameter_list|()
block|{
return|return
name|context
operator|.
name|reader
argument_list|()
operator|.
name|maxDoc
argument_list|()
return|;
block|}
block|}
return|;
block|}
block|}
return|;
block|}
block|}
annotation|@
name|Override
DECL|method|getBuilderPrototype
specifier|public
name|ScriptQueryBuilder
name|getBuilderPrototype
parameter_list|()
block|{
return|return
name|ScriptQueryBuilder
operator|.
name|PROTOTYPE
return|;
block|}
block|}
end_class

end_unit

