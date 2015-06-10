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
name|index
operator|.
name|Term
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
name|TermQuery
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
name|BytesRefs
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
name|mapper
operator|.
name|MappedFieldType
import|;
end_import

begin_comment
comment|/**  * A Query that matches documents containing a term.  */
end_comment

begin_class
DECL|class|TermQueryBuilder
specifier|public
class|class
name|TermQueryBuilder
extends|extends
name|BaseTermQueryBuilder
argument_list|<
name|TermQueryBuilder
argument_list|>
implements|implements
name|BoostableQueryBuilder
argument_list|<
name|TermQueryBuilder
argument_list|>
block|{
DECL|field|NAME
specifier|public
specifier|static
specifier|final
name|String
name|NAME
init|=
literal|"term"
decl_stmt|;
DECL|field|PROTOTYPE
specifier|static
specifier|final
name|TermQueryBuilder
name|PROTOTYPE
init|=
operator|new
name|TermQueryBuilder
argument_list|(
literal|null
argument_list|,
literal|null
argument_list|)
decl_stmt|;
comment|/** @see BaseTermQueryBuilder#BaseTermQueryBuilder(String, String) */
DECL|method|TermQueryBuilder
specifier|public
name|TermQueryBuilder
parameter_list|(
name|String
name|fieldName
parameter_list|,
name|String
name|value
parameter_list|)
block|{
name|super
argument_list|(
name|fieldName
argument_list|,
operator|(
name|Object
operator|)
name|value
argument_list|)
expr_stmt|;
block|}
comment|/** @see BaseTermQueryBuilder#BaseTermQueryBuilder(String, int) */
DECL|method|TermQueryBuilder
specifier|public
name|TermQueryBuilder
parameter_list|(
name|String
name|fieldName
parameter_list|,
name|int
name|value
parameter_list|)
block|{
name|super
argument_list|(
name|fieldName
argument_list|,
operator|(
name|Object
operator|)
name|value
argument_list|)
expr_stmt|;
block|}
comment|/** @see BaseTermQueryBuilder#BaseTermQueryBuilder(String, long) */
DECL|method|TermQueryBuilder
specifier|public
name|TermQueryBuilder
parameter_list|(
name|String
name|fieldName
parameter_list|,
name|long
name|value
parameter_list|)
block|{
name|super
argument_list|(
name|fieldName
argument_list|,
operator|(
name|Object
operator|)
name|value
argument_list|)
expr_stmt|;
block|}
comment|/** @see BaseTermQueryBuilder#BaseTermQueryBuilder(String, float) */
DECL|method|TermQueryBuilder
specifier|public
name|TermQueryBuilder
parameter_list|(
name|String
name|fieldName
parameter_list|,
name|float
name|value
parameter_list|)
block|{
name|super
argument_list|(
name|fieldName
argument_list|,
operator|(
name|Object
operator|)
name|value
argument_list|)
expr_stmt|;
block|}
comment|/** @see BaseTermQueryBuilder#BaseTermQueryBuilder(String, double) */
DECL|method|TermQueryBuilder
specifier|public
name|TermQueryBuilder
parameter_list|(
name|String
name|fieldName
parameter_list|,
name|double
name|value
parameter_list|)
block|{
name|super
argument_list|(
name|fieldName
argument_list|,
operator|(
name|Object
operator|)
name|value
argument_list|)
expr_stmt|;
block|}
comment|/** @see BaseTermQueryBuilder#BaseTermQueryBuilder(String, boolean) */
DECL|method|TermQueryBuilder
specifier|public
name|TermQueryBuilder
parameter_list|(
name|String
name|fieldName
parameter_list|,
name|boolean
name|value
parameter_list|)
block|{
name|super
argument_list|(
name|fieldName
argument_list|,
operator|(
name|Object
operator|)
name|value
argument_list|)
expr_stmt|;
block|}
comment|/** @see BaseTermQueryBuilder#BaseTermQueryBuilder(String, Object) */
DECL|method|TermQueryBuilder
specifier|public
name|TermQueryBuilder
parameter_list|(
name|String
name|fieldName
parameter_list|,
name|Object
name|value
parameter_list|)
block|{
name|super
argument_list|(
name|fieldName
argument_list|,
name|value
argument_list|)
expr_stmt|;
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
name|Query
name|query
init|=
literal|null
decl_stmt|;
name|MappedFieldType
name|mapper
init|=
name|parseContext
operator|.
name|fieldMapper
argument_list|(
name|this
operator|.
name|fieldName
argument_list|)
decl_stmt|;
if|if
condition|(
name|mapper
operator|!=
literal|null
condition|)
block|{
name|query
operator|=
name|mapper
operator|.
name|termQuery
argument_list|(
name|this
operator|.
name|value
argument_list|,
name|parseContext
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|query
operator|==
literal|null
condition|)
block|{
name|query
operator|=
operator|new
name|TermQuery
argument_list|(
operator|new
name|Term
argument_list|(
name|this
operator|.
name|fieldName
argument_list|,
name|BytesRefs
operator|.
name|toBytesRef
argument_list|(
name|this
operator|.
name|value
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
block|}
name|query
operator|.
name|setBoost
argument_list|(
name|this
operator|.
name|boost
argument_list|)
expr_stmt|;
if|if
condition|(
name|this
operator|.
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
return|return
name|query
return|;
block|}
annotation|@
name|Override
DECL|method|createBuilder
specifier|protected
name|TermQueryBuilder
name|createBuilder
parameter_list|(
name|String
name|fieldName
parameter_list|,
name|Object
name|value
parameter_list|)
block|{
return|return
operator|new
name|TermQueryBuilder
argument_list|(
name|fieldName
argument_list|,
name|value
argument_list|)
return|;
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

