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
name|apache
operator|.
name|lucene
operator|.
name|search
operator|.
name|MatchAllDocsQuery
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
name|Streamable
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
name|lucene
operator|.
name|search
operator|.
name|Queries
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
comment|/**  * A query that matches on all documents.  */
end_comment

begin_class
DECL|class|MatchAllQueryBuilder
specifier|public
class|class
name|MatchAllQueryBuilder
extends|extends
name|BaseQueryBuilder
implements|implements
name|Streamable
implements|,
name|BoostableQueryBuilder
argument_list|<
name|MatchAllQueryBuilder
argument_list|>
block|{
DECL|field|boost
specifier|private
name|float
name|boost
init|=
literal|1.0f
decl_stmt|;
comment|/**      * Sets the boost for this query.  Documents matching this query will (in addition to the normal      * weightings) have their score multiplied by the boost provided.      */
annotation|@
name|Override
DECL|method|boost
specifier|public
name|MatchAllQueryBuilder
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
comment|/**      * Gets the boost for this query.      */
DECL|method|boost
specifier|public
name|float
name|boost
parameter_list|()
block|{
return|return
name|this
operator|.
name|boost
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
name|MatchAllQueryParser
operator|.
name|NAME
argument_list|)
expr_stmt|;
if|if
condition|(
name|boost
operator|!=
literal|1.0f
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
name|builder
operator|.
name|endObject
argument_list|()
expr_stmt|;
block|}
DECL|method|parserName
specifier|final
specifier|protected
name|String
name|parserName
parameter_list|()
block|{
return|return
name|MatchAllQueryParser
operator|.
name|NAME
return|;
block|}
annotation|@
name|Override
DECL|method|toQuery
specifier|public
name|Query
name|toQuery
parameter_list|(
name|QueryParseContext
name|parseContext
parameter_list|)
block|{
if|if
condition|(
name|this
operator|.
name|boost
operator|==
literal|1.0f
condition|)
block|{
return|return
name|Queries
operator|.
name|newMatchAllQuery
argument_list|()
return|;
block|}
name|MatchAllDocsQuery
name|query
init|=
operator|new
name|MatchAllDocsQuery
argument_list|()
decl_stmt|;
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
block|{
return|return
literal|true
return|;
block|}
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
block|{
return|return
literal|false
return|;
block|}
name|MatchAllQueryBuilder
name|that
init|=
operator|(
name|MatchAllQueryBuilder
operator|)
name|o
decl_stmt|;
return|return
name|Float
operator|.
name|compare
argument_list|(
name|that
operator|.
name|boost
argument_list|,
name|boost
argument_list|)
operator|==
literal|0
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
name|boost
operator|!=
operator|+
literal|0.0f
condition|?
name|Float
operator|.
name|floatToIntBits
argument_list|(
name|boost
argument_list|)
else|:
literal|0
return|;
block|}
annotation|@
name|Override
DECL|method|readFrom
specifier|public
name|void
name|readFrom
parameter_list|(
name|StreamInput
name|in
parameter_list|)
throws|throws
name|IOException
block|{
name|this
operator|.
name|boost
operator|=
name|in
operator|.
name|readFloat
argument_list|()
expr_stmt|;
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
name|writeFloat
argument_list|(
name|this
operator|.
name|boost
argument_list|)
expr_stmt|;
block|}
block|}
end_class

end_unit

