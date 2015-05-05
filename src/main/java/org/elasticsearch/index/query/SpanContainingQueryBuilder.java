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

begin_comment
comment|/**  * Builder for {@link SpanContainingQuery}.  */
end_comment

begin_class
DECL|class|SpanContainingQueryBuilder
specifier|public
class|class
name|SpanContainingQueryBuilder
extends|extends
name|BaseQueryBuilder
implements|implements
name|SpanQueryBuilder
implements|,
name|BoostableQueryBuilder
argument_list|<
name|SpanContainingQueryBuilder
argument_list|>
block|{
DECL|field|big
specifier|private
name|SpanQueryBuilder
name|big
decl_stmt|;
DECL|field|little
specifier|private
name|SpanQueryBuilder
name|little
decl_stmt|;
DECL|field|boost
specifier|private
name|float
name|boost
init|=
operator|-
literal|1
decl_stmt|;
DECL|field|queryName
specifier|private
name|String
name|queryName
decl_stmt|;
comment|/**      * Sets the little clause, it must be contained within {@code big} for a match.      */
DECL|method|little
specifier|public
name|SpanContainingQueryBuilder
name|little
parameter_list|(
name|SpanQueryBuilder
name|clause
parameter_list|)
block|{
name|this
operator|.
name|little
operator|=
name|clause
expr_stmt|;
return|return
name|this
return|;
block|}
comment|/**      * Sets the big clause, it must enclose {@code little} for a match.      */
DECL|method|big
specifier|public
name|SpanContainingQueryBuilder
name|big
parameter_list|(
name|SpanQueryBuilder
name|clause
parameter_list|)
block|{
name|this
operator|.
name|big
operator|=
name|clause
expr_stmt|;
return|return
name|this
return|;
block|}
annotation|@
name|Override
DECL|method|boost
specifier|public
name|SpanContainingQueryBuilder
name|boost
parameter_list|(
name|float
name|boost
parameter_list|)
block|{
name|this
operator|.
name|boost
operator|=
name|boost
expr_stmt|;
return|return
name|this
return|;
block|}
comment|/**      * Sets the query name for the filter that can be used when searching for matched_filters per hit.      */
DECL|method|queryName
specifier|public
name|SpanContainingQueryBuilder
name|queryName
parameter_list|(
name|String
name|queryName
parameter_list|)
block|{
name|this
operator|.
name|queryName
operator|=
name|queryName
expr_stmt|;
return|return
name|this
return|;
block|}
annotation|@
name|Override
DECL|method|doXContent
specifier|protected
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
if|if
condition|(
name|big
operator|==
literal|null
condition|)
block|{
throw|throw
operator|new
name|IllegalArgumentException
argument_list|(
literal|"Must specify big clause when building a span_containing query"
argument_list|)
throw|;
block|}
if|if
condition|(
name|little
operator|==
literal|null
condition|)
block|{
throw|throw
operator|new
name|IllegalArgumentException
argument_list|(
literal|"Must specify little clause when building a span_containing query"
argument_list|)
throw|;
block|}
name|builder
operator|.
name|startObject
argument_list|(
name|SpanContainingQueryParser
operator|.
name|NAME
argument_list|)
expr_stmt|;
name|builder
operator|.
name|field
argument_list|(
literal|"big"
argument_list|)
expr_stmt|;
name|big
operator|.
name|toXContent
argument_list|(
name|builder
argument_list|,
name|params
argument_list|)
expr_stmt|;
name|builder
operator|.
name|field
argument_list|(
literal|"little"
argument_list|)
expr_stmt|;
name|little
operator|.
name|toXContent
argument_list|(
name|builder
argument_list|,
name|params
argument_list|)
expr_stmt|;
if|if
condition|(
name|boost
operator|!=
operator|-
literal|1
condition|)
block|{
name|builder
operator|.
name|field
argument_list|(
literal|"boost"
argument_list|,
name|boost
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
name|builder
operator|.
name|endObject
argument_list|()
expr_stmt|;
block|}
annotation|@
name|Override
DECL|method|parserName
specifier|protected
name|String
name|parserName
parameter_list|()
block|{
return|return
name|SpanContainingQueryParser
operator|.
name|NAME
return|;
block|}
block|}
end_class

end_unit

