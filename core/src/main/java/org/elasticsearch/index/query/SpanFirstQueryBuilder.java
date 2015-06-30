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

begin_class
DECL|class|SpanFirstQueryBuilder
specifier|public
class|class
name|SpanFirstQueryBuilder
extends|extends
name|AbstractQueryBuilder
argument_list|<
name|SpanFirstQueryBuilder
argument_list|>
implements|implements
name|SpanQueryBuilder
argument_list|<
name|SpanFirstQueryBuilder
argument_list|>
block|{
DECL|field|NAME
specifier|public
specifier|static
specifier|final
name|String
name|NAME
init|=
literal|"span_first"
decl_stmt|;
DECL|field|matchBuilder
specifier|private
specifier|final
name|SpanQueryBuilder
name|matchBuilder
decl_stmt|;
DECL|field|end
specifier|private
specifier|final
name|int
name|end
decl_stmt|;
DECL|field|SPAN_FIRST_QUERY_BUILDER
specifier|static
specifier|final
name|SpanFirstQueryBuilder
name|SPAN_FIRST_QUERY_BUILDER
init|=
operator|new
name|SpanFirstQueryBuilder
argument_list|(
literal|null
argument_list|,
operator|-
literal|1
argument_list|)
decl_stmt|;
DECL|method|SpanFirstQueryBuilder
specifier|public
name|SpanFirstQueryBuilder
parameter_list|(
name|SpanQueryBuilder
name|matchBuilder
parameter_list|,
name|int
name|end
parameter_list|)
block|{
name|this
operator|.
name|matchBuilder
operator|=
name|matchBuilder
expr_stmt|;
name|this
operator|.
name|end
operator|=
name|end
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
literal|"match"
argument_list|)
expr_stmt|;
name|matchBuilder
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
literal|"end"
argument_list|,
name|end
argument_list|)
expr_stmt|;
name|printBoostAndQueryName
argument_list|(
name|builder
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
DECL|method|getName
specifier|public
name|String
name|getName
parameter_list|()
block|{
return|return
name|NAME
return|;
block|}
block|}
end_class

end_unit

