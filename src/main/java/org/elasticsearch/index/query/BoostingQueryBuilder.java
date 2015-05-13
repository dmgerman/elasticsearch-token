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
comment|/**  * The BoostingQuery class can be used to effectively demote results that match a given query.  * Unlike the "NOT" clause, this still selects documents that contain undesirable terms,  * but reduces their overall score:  *<p/>  * Query balancedQuery = new BoostingQuery(positiveQuery, negativeQuery, 0.01f);  * In this scenario the positiveQuery contains the mandatory, desirable criteria which is used to  * select all matching documents, and the negativeQuery contains the undesirable elements which  * are simply used to lessen the scores. Documents that match the negativeQuery have their score  * multiplied by the supplied "boost" parameter, so this should be less than 1 to achieve a  * demoting effect  */
end_comment

begin_class
DECL|class|BoostingQueryBuilder
specifier|public
class|class
name|BoostingQueryBuilder
extends|extends
name|QueryBuilder
implements|implements
name|BoostableQueryBuilder
argument_list|<
name|BoostingQueryBuilder
argument_list|>
block|{
DECL|field|positiveQuery
specifier|private
name|QueryBuilder
name|positiveQuery
decl_stmt|;
DECL|field|negativeQuery
specifier|private
name|QueryBuilder
name|negativeQuery
decl_stmt|;
DECL|field|negativeBoost
specifier|private
name|float
name|negativeBoost
init|=
operator|-
literal|1
decl_stmt|;
DECL|field|boost
specifier|private
name|float
name|boost
init|=
operator|-
literal|1
decl_stmt|;
DECL|method|BoostingQueryBuilder
specifier|public
name|BoostingQueryBuilder
parameter_list|()
block|{      }
DECL|method|positive
specifier|public
name|BoostingQueryBuilder
name|positive
parameter_list|(
name|QueryBuilder
name|positiveQuery
parameter_list|)
block|{
name|this
operator|.
name|positiveQuery
operator|=
name|positiveQuery
expr_stmt|;
return|return
name|this
return|;
block|}
DECL|method|negative
specifier|public
name|BoostingQueryBuilder
name|negative
parameter_list|(
name|QueryBuilder
name|negativeQuery
parameter_list|)
block|{
name|this
operator|.
name|negativeQuery
operator|=
name|negativeQuery
expr_stmt|;
return|return
name|this
return|;
block|}
DECL|method|negativeBoost
specifier|public
name|BoostingQueryBuilder
name|negativeBoost
parameter_list|(
name|float
name|negativeBoost
parameter_list|)
block|{
name|this
operator|.
name|negativeBoost
operator|=
name|negativeBoost
expr_stmt|;
return|return
name|this
return|;
block|}
annotation|@
name|Override
DECL|method|boost
specifier|public
name|BoostingQueryBuilder
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
name|positiveQuery
operator|==
literal|null
condition|)
block|{
throw|throw
operator|new
name|IllegalArgumentException
argument_list|(
literal|"boosting query requires positive query to be set"
argument_list|)
throw|;
block|}
if|if
condition|(
name|negativeQuery
operator|==
literal|null
condition|)
block|{
throw|throw
operator|new
name|IllegalArgumentException
argument_list|(
literal|"boosting query requires negative query to be set"
argument_list|)
throw|;
block|}
if|if
condition|(
name|negativeBoost
operator|==
operator|-
literal|1
condition|)
block|{
throw|throw
operator|new
name|IllegalArgumentException
argument_list|(
literal|"boosting query requires negativeBoost to be set"
argument_list|)
throw|;
block|}
name|builder
operator|.
name|startObject
argument_list|(
name|BoostingQueryParser
operator|.
name|NAME
argument_list|)
expr_stmt|;
name|builder
operator|.
name|field
argument_list|(
literal|"positive"
argument_list|)
expr_stmt|;
name|positiveQuery
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
literal|"negative"
argument_list|)
expr_stmt|;
name|negativeQuery
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
literal|"negative_boost"
argument_list|,
name|negativeBoost
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
name|builder
operator|.
name|endObject
argument_list|()
expr_stmt|;
block|}
block|}
end_class

end_unit

