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
name|org
operator|.
name|elasticsearch
operator|.
name|common
operator|.
name|xcontent
operator|.
name|XContentBuilder
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
name|HashMap
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|Locale
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

begin_comment
comment|/**  * SimpleQuery is a query parser that acts similar to a query_string  * query, but won't throw exceptions for any weird string syntax.  */
end_comment

begin_class
DECL|class|SimpleQueryStringBuilder
specifier|public
class|class
name|SimpleQueryStringBuilder
extends|extends
name|QueryBuilder
block|{
DECL|field|NAME
specifier|public
specifier|static
specifier|final
name|String
name|NAME
init|=
literal|"simple_query_string"
decl_stmt|;
DECL|field|fields
specifier|private
name|Map
argument_list|<
name|String
argument_list|,
name|Float
argument_list|>
name|fields
init|=
operator|new
name|HashMap
argument_list|<>
argument_list|()
decl_stmt|;
DECL|field|analyzer
specifier|private
name|String
name|analyzer
decl_stmt|;
DECL|field|operator
specifier|private
name|Operator
name|operator
decl_stmt|;
DECL|field|queryText
specifier|private
specifier|final
name|String
name|queryText
decl_stmt|;
DECL|field|queryName
specifier|private
name|String
name|queryName
decl_stmt|;
DECL|field|minimumShouldMatch
specifier|private
name|String
name|minimumShouldMatch
decl_stmt|;
DECL|field|flags
specifier|private
name|int
name|flags
init|=
operator|-
literal|1
decl_stmt|;
DECL|field|lowercaseExpandedTerms
specifier|private
name|Boolean
name|lowercaseExpandedTerms
decl_stmt|;
DECL|field|lenient
specifier|private
name|Boolean
name|lenient
decl_stmt|;
DECL|field|analyzeWildcard
specifier|private
name|Boolean
name|analyzeWildcard
decl_stmt|;
DECL|field|locale
specifier|private
name|Locale
name|locale
decl_stmt|;
DECL|field|PROTOTYPE
specifier|static
specifier|final
name|SimpleQueryStringBuilder
name|PROTOTYPE
init|=
operator|new
name|SimpleQueryStringBuilder
argument_list|(
literal|null
argument_list|)
decl_stmt|;
comment|/**      * Operators for the default_operator      */
DECL|enum|Operator
specifier|public
specifier|static
enum|enum
name|Operator
block|{
DECL|enum constant|AND
name|AND
block|,
DECL|enum constant|OR
name|OR
block|}
comment|/**      * Construct a new simple query with the given text      */
DECL|method|SimpleQueryStringBuilder
specifier|public
name|SimpleQueryStringBuilder
parameter_list|(
name|String
name|text
parameter_list|)
block|{
name|this
operator|.
name|queryText
operator|=
name|text
expr_stmt|;
block|}
comment|/**      * Add a field to run the query against      */
DECL|method|field
specifier|public
name|SimpleQueryStringBuilder
name|field
parameter_list|(
name|String
name|field
parameter_list|)
block|{
name|this
operator|.
name|fields
operator|.
name|put
argument_list|(
name|field
argument_list|,
literal|null
argument_list|)
expr_stmt|;
return|return
name|this
return|;
block|}
comment|/**      * Add a field to run the query against with a specific boost      */
DECL|method|field
specifier|public
name|SimpleQueryStringBuilder
name|field
parameter_list|(
name|String
name|field
parameter_list|,
name|float
name|boost
parameter_list|)
block|{
name|this
operator|.
name|fields
operator|.
name|put
argument_list|(
name|field
argument_list|,
name|boost
argument_list|)
expr_stmt|;
return|return
name|this
return|;
block|}
comment|/**      * Specify a name for the query      */
DECL|method|queryName
specifier|public
name|SimpleQueryStringBuilder
name|queryName
parameter_list|(
name|String
name|name
parameter_list|)
block|{
name|this
operator|.
name|queryName
operator|=
name|name
expr_stmt|;
return|return
name|this
return|;
block|}
comment|/**      * Specify an analyzer to use for the query      */
DECL|method|analyzer
specifier|public
name|SimpleQueryStringBuilder
name|analyzer
parameter_list|(
name|String
name|analyzer
parameter_list|)
block|{
name|this
operator|.
name|analyzer
operator|=
name|analyzer
expr_stmt|;
return|return
name|this
return|;
block|}
comment|/**      * Specify the default operator for the query. Defaults to "OR" if no      * operator is specified      */
DECL|method|defaultOperator
specifier|public
name|SimpleQueryStringBuilder
name|defaultOperator
parameter_list|(
name|Operator
name|defaultOperator
parameter_list|)
block|{
name|this
operator|.
name|operator
operator|=
name|defaultOperator
expr_stmt|;
return|return
name|this
return|;
block|}
comment|/**      * Specify the enabled features of the SimpleQueryString.      */
DECL|method|flags
specifier|public
name|SimpleQueryStringBuilder
name|flags
parameter_list|(
name|SimpleQueryStringFlag
modifier|...
name|flags
parameter_list|)
block|{
name|int
name|value
init|=
literal|0
decl_stmt|;
if|if
condition|(
name|flags
operator|.
name|length
operator|==
literal|0
condition|)
block|{
name|value
operator|=
name|SimpleQueryStringFlag
operator|.
name|ALL
operator|.
name|value
expr_stmt|;
block|}
else|else
block|{
for|for
control|(
name|SimpleQueryStringFlag
name|flag
range|:
name|flags
control|)
block|{
name|value
operator||=
name|flag
operator|.
name|value
expr_stmt|;
block|}
block|}
name|this
operator|.
name|flags
operator|=
name|value
expr_stmt|;
return|return
name|this
return|;
block|}
DECL|method|lowercaseExpandedTerms
specifier|public
name|SimpleQueryStringBuilder
name|lowercaseExpandedTerms
parameter_list|(
name|boolean
name|lowercaseExpandedTerms
parameter_list|)
block|{
name|this
operator|.
name|lowercaseExpandedTerms
operator|=
name|lowercaseExpandedTerms
expr_stmt|;
return|return
name|this
return|;
block|}
DECL|method|locale
specifier|public
name|SimpleQueryStringBuilder
name|locale
parameter_list|(
name|Locale
name|locale
parameter_list|)
block|{
name|this
operator|.
name|locale
operator|=
name|locale
expr_stmt|;
return|return
name|this
return|;
block|}
DECL|method|lenient
specifier|public
name|SimpleQueryStringBuilder
name|lenient
parameter_list|(
name|boolean
name|lenient
parameter_list|)
block|{
name|this
operator|.
name|lenient
operator|=
name|lenient
expr_stmt|;
return|return
name|this
return|;
block|}
DECL|method|analyzeWildcard
specifier|public
name|SimpleQueryStringBuilder
name|analyzeWildcard
parameter_list|(
name|boolean
name|analyzeWildcard
parameter_list|)
block|{
name|this
operator|.
name|analyzeWildcard
operator|=
name|analyzeWildcard
expr_stmt|;
return|return
name|this
return|;
block|}
DECL|method|minimumShouldMatch
specifier|public
name|SimpleQueryStringBuilder
name|minimumShouldMatch
parameter_list|(
name|String
name|minimumShouldMatch
parameter_list|)
block|{
name|this
operator|.
name|minimumShouldMatch
operator|=
name|minimumShouldMatch
expr_stmt|;
return|return
name|this
return|;
block|}
annotation|@
name|Override
DECL|method|doXContent
specifier|public
name|void
name|doXContent
parameter_list|(
name|XContentBuilder
name|builder
parameter_list|,
name|Params
name|params
parameter_list|)
throws|throws
name|IOException
block|{
name|builder
operator|.
name|startObject
argument_list|(
name|NAME
argument_list|)
expr_stmt|;
name|builder
operator|.
name|field
argument_list|(
literal|"query"
argument_list|,
name|queryText
argument_list|)
expr_stmt|;
if|if
condition|(
name|fields
operator|.
name|size
argument_list|()
operator|>
literal|0
condition|)
block|{
name|builder
operator|.
name|startArray
argument_list|(
literal|"fields"
argument_list|)
expr_stmt|;
for|for
control|(
name|Map
operator|.
name|Entry
argument_list|<
name|String
argument_list|,
name|Float
argument_list|>
name|entry
range|:
name|fields
operator|.
name|entrySet
argument_list|()
control|)
block|{
name|String
name|field
init|=
name|entry
operator|.
name|getKey
argument_list|()
decl_stmt|;
name|Float
name|boost
init|=
name|entry
operator|.
name|getValue
argument_list|()
decl_stmt|;
if|if
condition|(
name|boost
operator|!=
literal|null
condition|)
block|{
name|builder
operator|.
name|value
argument_list|(
name|field
operator|+
literal|"^"
operator|+
name|boost
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|builder
operator|.
name|value
argument_list|(
name|field
argument_list|)
expr_stmt|;
block|}
block|}
name|builder
operator|.
name|endArray
argument_list|()
expr_stmt|;
block|}
if|if
condition|(
name|flags
operator|!=
operator|-
literal|1
condition|)
block|{
name|builder
operator|.
name|field
argument_list|(
literal|"flags"
argument_list|,
name|flags
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|analyzer
operator|!=
literal|null
condition|)
block|{
name|builder
operator|.
name|field
argument_list|(
literal|"analyzer"
argument_list|,
name|analyzer
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|operator
operator|!=
literal|null
condition|)
block|{
name|builder
operator|.
name|field
argument_list|(
literal|"default_operator"
argument_list|,
name|operator
operator|.
name|name
argument_list|()
operator|.
name|toLowerCase
argument_list|(
name|Locale
operator|.
name|ROOT
argument_list|)
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|lowercaseExpandedTerms
operator|!=
literal|null
condition|)
block|{
name|builder
operator|.
name|field
argument_list|(
literal|"lowercase_expanded_terms"
argument_list|,
name|lowercaseExpandedTerms
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|lenient
operator|!=
literal|null
condition|)
block|{
name|builder
operator|.
name|field
argument_list|(
literal|"lenient"
argument_list|,
name|lenient
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|analyzeWildcard
operator|!=
literal|null
condition|)
block|{
name|builder
operator|.
name|field
argument_list|(
literal|"analyze_wildcard"
argument_list|,
name|analyzeWildcard
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|locale
operator|!=
literal|null
condition|)
block|{
name|builder
operator|.
name|field
argument_list|(
literal|"locale"
argument_list|,
name|locale
operator|.
name|toString
argument_list|()
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|queryName
operator|!=
literal|null
condition|)
block|{
name|builder
operator|.
name|field
argument_list|(
literal|"_name"
argument_list|,
name|queryName
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|minimumShouldMatch
operator|!=
literal|null
condition|)
block|{
name|builder
operator|.
name|field
argument_list|(
literal|"minimum_should_match"
argument_list|,
name|minimumShouldMatch
argument_list|)
expr_stmt|;
block|}
name|builder
operator|.
name|endObject
argument_list|()
expr_stmt|;
block|}
annotation|@
name|Override
DECL|method|queryId
specifier|public
name|String
name|queryId
parameter_list|()
block|{
return|return
name|NAME
return|;
block|}
block|}
end_class

end_unit
