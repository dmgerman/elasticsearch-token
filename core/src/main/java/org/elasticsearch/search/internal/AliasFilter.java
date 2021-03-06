begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to Elasticsearch under one or more contributor  * license agreements. See the NOTICE file distributed with  * this work for additional information regarding copyright  * ownership. Elasticsearch licenses this file to you under  * the Apache License, Version 2.0 (the "License"); you may  * not use this file except in compliance with the License.  * You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.search.internal
package|package
name|org
operator|.
name|elasticsearch
operator|.
name|search
operator|.
name|internal
package|;
end_package

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|Version
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|cluster
operator|.
name|metadata
operator|.
name|IndexMetaData
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
name|CheckedFunction
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
name|Strings
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
name|io
operator|.
name|stream
operator|.
name|StreamInput
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
name|io
operator|.
name|stream
operator|.
name|StreamOutput
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
name|io
operator|.
name|stream
operator|.
name|Writeable
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
name|XContentFactory
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
name|QueryBuilder
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
name|QueryRewriteContext
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
name|Arrays
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

begin_comment
comment|/**  * Represents a {@link QueryBuilder} and a list of alias names that filters the builder is composed of.  */
end_comment

begin_class
DECL|class|AliasFilter
specifier|public
specifier|final
class|class
name|AliasFilter
implements|implements
name|Writeable
block|{
DECL|field|aliases
specifier|private
specifier|final
name|String
index|[]
name|aliases
decl_stmt|;
DECL|field|filter
specifier|private
specifier|final
name|QueryBuilder
name|filter
decl_stmt|;
DECL|field|reparseAliases
specifier|private
specifier|final
name|boolean
name|reparseAliases
decl_stmt|;
DECL|field|EMPTY
specifier|public
specifier|static
specifier|final
name|AliasFilter
name|EMPTY
init|=
operator|new
name|AliasFilter
argument_list|(
literal|null
argument_list|,
name|Strings
operator|.
name|EMPTY_ARRAY
argument_list|)
decl_stmt|;
DECL|method|AliasFilter
specifier|public
name|AliasFilter
parameter_list|(
name|QueryBuilder
name|filter
parameter_list|,
name|String
modifier|...
name|aliases
parameter_list|)
block|{
name|this
operator|.
name|aliases
operator|=
name|aliases
operator|==
literal|null
condition|?
name|Strings
operator|.
name|EMPTY_ARRAY
else|:
name|aliases
expr_stmt|;
name|this
operator|.
name|filter
operator|=
name|filter
expr_stmt|;
name|reparseAliases
operator|=
literal|false
expr_stmt|;
comment|// no bwc here - we only do this if we parse the filter
block|}
DECL|method|AliasFilter
specifier|public
name|AliasFilter
parameter_list|(
name|StreamInput
name|input
parameter_list|)
throws|throws
name|IOException
block|{
name|aliases
operator|=
name|input
operator|.
name|readStringArray
argument_list|()
expr_stmt|;
if|if
condition|(
name|input
operator|.
name|getVersion
argument_list|()
operator|.
name|onOrAfter
argument_list|(
name|Version
operator|.
name|V_5_1_1
argument_list|)
condition|)
block|{
name|filter
operator|=
name|input
operator|.
name|readOptionalNamedWriteable
argument_list|(
name|QueryBuilder
operator|.
name|class
argument_list|)
expr_stmt|;
name|reparseAliases
operator|=
literal|false
expr_stmt|;
block|}
else|else
block|{
name|reparseAliases
operator|=
literal|true
expr_stmt|;
comment|// alright we read from 5.0
name|filter
operator|=
literal|null
expr_stmt|;
block|}
block|}
DECL|method|reparseFilter
specifier|private
name|QueryBuilder
name|reparseFilter
parameter_list|(
name|QueryRewriteContext
name|context
parameter_list|)
block|{
if|if
condition|(
name|reparseAliases
condition|)
block|{
comment|// we are processing a filter received from a 5.0 node - we need to reparse this on the executing node
specifier|final
name|IndexMetaData
name|indexMetaData
init|=
name|context
operator|.
name|getIndexSettings
argument_list|()
operator|.
name|getIndexMetaData
argument_list|()
decl_stmt|;
comment|/* Being static, parseAliasFilter doesn't have access to whatever guts it needs to parse a query. Instead of passing in a bunch              * of dependencies we pass in a function that can perform the parsing. */
name|CheckedFunction
argument_list|<
name|byte
index|[]
argument_list|,
name|QueryBuilder
argument_list|,
name|IOException
argument_list|>
name|filterParser
init|=
name|bytes
lambda|->
block|{
try|try
init|(
name|XContentParser
name|parser
init|=
name|XContentFactory
operator|.
name|xContent
argument_list|(
name|bytes
argument_list|)
operator|.
name|createParser
argument_list|(
name|context
operator|.
name|getXContentRegistry
argument_list|()
argument_list|,
name|bytes
argument_list|)
init|)
block|{
return|return
name|context
operator|.
name|newParseContext
argument_list|(
name|parser
argument_list|)
operator|.
name|parseInnerQueryBuilder
argument_list|()
return|;
block|}
block|}
decl_stmt|;
return|return
name|ShardSearchRequest
operator|.
name|parseAliasFilter
argument_list|(
name|filterParser
argument_list|,
name|indexMetaData
argument_list|,
name|aliases
argument_list|)
return|;
block|}
return|return
name|filter
return|;
block|}
DECL|method|rewrite
name|AliasFilter
name|rewrite
parameter_list|(
name|QueryRewriteContext
name|context
parameter_list|)
throws|throws
name|IOException
block|{
name|QueryBuilder
name|queryBuilder
init|=
name|reparseFilter
argument_list|(
name|context
argument_list|)
decl_stmt|;
if|if
condition|(
name|queryBuilder
operator|!=
literal|null
condition|)
block|{
return|return
operator|new
name|AliasFilter
argument_list|(
name|QueryBuilder
operator|.
name|rewriteQuery
argument_list|(
name|queryBuilder
argument_list|,
name|context
argument_list|)
argument_list|,
name|aliases
argument_list|)
return|;
block|}
return|return
operator|new
name|AliasFilter
argument_list|(
name|filter
argument_list|,
name|aliases
argument_list|)
return|;
block|}
annotation|@
name|Override
DECL|method|writeTo
specifier|public
name|void
name|writeTo
parameter_list|(
name|StreamOutput
name|out
parameter_list|)
throws|throws
name|IOException
block|{
name|out
operator|.
name|writeStringArray
argument_list|(
name|aliases
argument_list|)
expr_stmt|;
if|if
condition|(
name|out
operator|.
name|getVersion
argument_list|()
operator|.
name|onOrAfter
argument_list|(
name|Version
operator|.
name|V_5_1_1
argument_list|)
condition|)
block|{
name|out
operator|.
name|writeOptionalNamedWriteable
argument_list|(
name|filter
argument_list|)
expr_stmt|;
block|}
block|}
comment|/**      * Returns the aliases patters that are used to compose the {@link QueryBuilder}      * returned from {@link #getQueryBuilder()}      */
DECL|method|getAliases
specifier|public
name|String
index|[]
name|getAliases
parameter_list|()
block|{
return|return
name|aliases
return|;
block|}
comment|/**      * Returns the alias filter {@link QueryBuilder} or<code>null</code> if there is no such filter      */
DECL|method|getQueryBuilder
specifier|public
name|QueryBuilder
name|getQueryBuilder
parameter_list|()
block|{
if|if
condition|(
name|reparseAliases
condition|)
block|{
comment|// this is only for BWC since 5.0 still  only sends aliases so this must be rewritten on the executing node
comment|// if we talk to an older node we also only forward/write the string array which is compatible with the consumers
comment|// in 5.0 see ExplainRequest and QueryValidationRequest
throw|throw
operator|new
name|IllegalStateException
argument_list|(
literal|"alias filter for aliases: "
operator|+
name|Arrays
operator|.
name|toString
argument_list|(
name|aliases
argument_list|)
operator|+
literal|" must be rewritten first"
argument_list|)
throw|;
block|}
return|return
name|filter
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
name|o
parameter_list|)
block|{
if|if
condition|(
name|this
operator|==
name|o
condition|)
return|return
literal|true
return|;
if|if
condition|(
name|o
operator|==
literal|null
operator|||
name|getClass
argument_list|()
operator|!=
name|o
operator|.
name|getClass
argument_list|()
condition|)
return|return
literal|false
return|;
name|AliasFilter
name|that
init|=
operator|(
name|AliasFilter
operator|)
name|o
decl_stmt|;
return|return
name|reparseAliases
operator|==
name|that
operator|.
name|reparseAliases
operator|&&
name|Arrays
operator|.
name|equals
argument_list|(
name|aliases
argument_list|,
name|that
operator|.
name|aliases
argument_list|)
operator|&&
name|Objects
operator|.
name|equals
argument_list|(
name|filter
argument_list|,
name|that
operator|.
name|filter
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
return|return
name|Objects
operator|.
name|hash
argument_list|(
name|reparseAliases
argument_list|,
name|Arrays
operator|.
name|hashCode
argument_list|(
name|aliases
argument_list|)
argument_list|,
name|filter
argument_list|)
return|;
block|}
annotation|@
name|Override
DECL|method|toString
specifier|public
name|String
name|toString
parameter_list|()
block|{
return|return
literal|"AliasFilter{"
operator|+
literal|"aliases="
operator|+
name|Arrays
operator|.
name|toString
argument_list|(
name|aliases
argument_list|)
operator|+
literal|", filter="
operator|+
name|filter
operator|+
literal|", reparseAliases="
operator|+
name|reparseAliases
operator|+
literal|'}'
return|;
block|}
block|}
end_class

end_unit

