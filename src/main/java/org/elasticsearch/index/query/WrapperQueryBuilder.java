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
name|Charsets
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
name|bytes
operator|.
name|BytesReference
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
comment|/**  * A Query builder which allows building a query given JSON string or binary data provided as input. This is useful when you want  * to use the Java Builder API but still have JSON query strings at hand that you want to combine with other  * query builders.  *<p/>  * Example usage in a boolean query :  *<pre>  * {@code  *      BoolQueryBuilder bool = new BoolQueryBuilder();  *      bool.must(new WrapperQueryBuilder("{\"term\": {\"field\":\"value\"}}");  *      bool.must(new TermQueryBuilder("field2","value2");  * }  *</pre>  */
end_comment

begin_class
DECL|class|WrapperQueryBuilder
specifier|public
class|class
name|WrapperQueryBuilder
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
literal|"wrapper"
decl_stmt|;
DECL|field|source
specifier|private
specifier|final
name|byte
index|[]
name|source
decl_stmt|;
DECL|field|offset
specifier|private
specifier|final
name|int
name|offset
decl_stmt|;
DECL|field|length
specifier|private
specifier|final
name|int
name|length
decl_stmt|;
comment|/**      * Creates a query builder given a query provided as a string      */
DECL|method|WrapperQueryBuilder
specifier|public
name|WrapperQueryBuilder
parameter_list|(
name|String
name|source
parameter_list|)
block|{
name|this
operator|.
name|source
operator|=
name|source
operator|.
name|getBytes
argument_list|(
name|Charsets
operator|.
name|UTF_8
argument_list|)
expr_stmt|;
name|this
operator|.
name|offset
operator|=
literal|0
expr_stmt|;
name|this
operator|.
name|length
operator|=
name|this
operator|.
name|source
operator|.
name|length
expr_stmt|;
block|}
comment|/**      * Creates a query builder given a query provided as a bytes array      */
DECL|method|WrapperQueryBuilder
specifier|public
name|WrapperQueryBuilder
parameter_list|(
name|byte
index|[]
name|source
parameter_list|,
name|int
name|offset
parameter_list|,
name|int
name|length
parameter_list|)
block|{
name|this
operator|.
name|source
operator|=
name|source
expr_stmt|;
name|this
operator|.
name|offset
operator|=
name|offset
expr_stmt|;
name|this
operator|.
name|length
operator|=
name|length
expr_stmt|;
block|}
comment|/**      * Creates a query builder given a query provided as a {@link BytesReference}      */
DECL|method|WrapperQueryBuilder
specifier|public
name|WrapperQueryBuilder
parameter_list|(
name|BytesReference
name|source
parameter_list|)
block|{
name|this
operator|.
name|source
operator|=
name|source
operator|.
name|array
argument_list|()
expr_stmt|;
name|this
operator|.
name|offset
operator|=
name|source
operator|.
name|arrayOffset
argument_list|()
expr_stmt|;
name|this
operator|.
name|length
operator|=
name|source
operator|.
name|length
argument_list|()
expr_stmt|;
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
name|source
argument_list|,
name|offset
argument_list|,
name|length
argument_list|)
expr_stmt|;
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

