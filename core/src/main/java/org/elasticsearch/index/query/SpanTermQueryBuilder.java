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
name|spans
operator|.
name|SpanQuery
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
name|spans
operator|.
name|SpanTermQuery
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
name|BytesRef
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
comment|/**  * A Span Query that matches documents containing a term.  * @see SpanTermQuery  */
end_comment

begin_class
DECL|class|SpanTermQueryBuilder
specifier|public
class|class
name|SpanTermQueryBuilder
extends|extends
name|BaseTermQueryBuilder
argument_list|<
name|SpanTermQueryBuilder
argument_list|>
implements|implements
name|SpanQueryBuilder
argument_list|<
name|SpanTermQueryBuilder
argument_list|>
block|{
DECL|field|NAME
specifier|public
specifier|static
specifier|final
name|String
name|NAME
init|=
literal|"span_term"
decl_stmt|;
DECL|field|PROTOTYPE
specifier|static
specifier|final
name|SpanTermQueryBuilder
name|PROTOTYPE
init|=
operator|new
name|SpanTermQueryBuilder
argument_list|(
literal|"name"
argument_list|,
literal|"value"
argument_list|)
decl_stmt|;
comment|/** @see BaseTermQueryBuilder#BaseTermQueryBuilder(String, String) */
DECL|method|SpanTermQueryBuilder
specifier|public
name|SpanTermQueryBuilder
parameter_list|(
name|String
name|name
parameter_list|,
name|String
name|value
parameter_list|)
block|{
name|super
argument_list|(
name|name
argument_list|,
operator|(
name|Object
operator|)
name|value
argument_list|)
expr_stmt|;
block|}
comment|/** @see BaseTermQueryBuilder#BaseTermQueryBuilder(String, int) */
DECL|method|SpanTermQueryBuilder
specifier|public
name|SpanTermQueryBuilder
parameter_list|(
name|String
name|name
parameter_list|,
name|int
name|value
parameter_list|)
block|{
name|super
argument_list|(
name|name
argument_list|,
operator|(
name|Object
operator|)
name|value
argument_list|)
expr_stmt|;
block|}
comment|/** @see BaseTermQueryBuilder#BaseTermQueryBuilder(String, long) */
DECL|method|SpanTermQueryBuilder
specifier|public
name|SpanTermQueryBuilder
parameter_list|(
name|String
name|name
parameter_list|,
name|long
name|value
parameter_list|)
block|{
name|super
argument_list|(
name|name
argument_list|,
operator|(
name|Object
operator|)
name|value
argument_list|)
expr_stmt|;
block|}
comment|/** @see BaseTermQueryBuilder#BaseTermQueryBuilder(String, float) */
DECL|method|SpanTermQueryBuilder
specifier|public
name|SpanTermQueryBuilder
parameter_list|(
name|String
name|name
parameter_list|,
name|float
name|value
parameter_list|)
block|{
name|super
argument_list|(
name|name
argument_list|,
operator|(
name|Object
operator|)
name|value
argument_list|)
expr_stmt|;
block|}
comment|/** @see BaseTermQueryBuilder#BaseTermQueryBuilder(String, double) */
DECL|method|SpanTermQueryBuilder
specifier|public
name|SpanTermQueryBuilder
parameter_list|(
name|String
name|name
parameter_list|,
name|double
name|value
parameter_list|)
block|{
name|super
argument_list|(
name|name
argument_list|,
operator|(
name|Object
operator|)
name|value
argument_list|)
expr_stmt|;
block|}
comment|/** @see BaseTermQueryBuilder#BaseTermQueryBuilder(String, Object) */
DECL|method|SpanTermQueryBuilder
specifier|public
name|SpanTermQueryBuilder
parameter_list|(
name|String
name|name
parameter_list|,
name|Object
name|value
parameter_list|)
block|{
name|super
argument_list|(
name|name
argument_list|,
name|value
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
DECL|method|doToQuery
specifier|protected
name|SpanQuery
name|doToQuery
parameter_list|(
name|QueryShardContext
name|context
parameter_list|)
throws|throws
name|IOException
block|{
name|BytesRef
name|valueBytes
init|=
literal|null
decl_stmt|;
name|String
name|fieldName
init|=
name|this
operator|.
name|fieldName
decl_stmt|;
name|MappedFieldType
name|mapper
init|=
name|context
operator|.
name|fieldMapper
argument_list|(
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
name|fieldName
operator|=
name|mapper
operator|.
name|name
argument_list|()
expr_stmt|;
name|valueBytes
operator|=
name|mapper
operator|.
name|indexedValueForSearch
argument_list|(
name|value
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|valueBytes
operator|==
literal|null
condition|)
block|{
name|valueBytes
operator|=
name|BytesRefs
operator|.
name|toBytesRef
argument_list|(
name|this
operator|.
name|value
argument_list|)
expr_stmt|;
block|}
return|return
operator|new
name|SpanTermQuery
argument_list|(
operator|new
name|Term
argument_list|(
name|fieldName
argument_list|,
name|valueBytes
argument_list|)
argument_list|)
return|;
block|}
annotation|@
name|Override
DECL|method|createBuilder
specifier|protected
name|SpanTermQueryBuilder
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
name|SpanTermQueryBuilder
argument_list|(
name|fieldName
argument_list|,
name|value
argument_list|)
return|;
block|}
annotation|@
name|Override
DECL|method|getWriteableName
specifier|public
name|String
name|getWriteableName
parameter_list|()
block|{
return|return
name|NAME
return|;
block|}
block|}
end_class

end_unit

