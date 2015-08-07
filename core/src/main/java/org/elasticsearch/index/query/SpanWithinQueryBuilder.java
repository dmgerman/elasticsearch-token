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
name|SpanWithinQuery
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
name|Objects
import|;
end_import

begin_comment
comment|/**  * Builder for {@link org.apache.lucene.search.spans.SpanWithinQuery}.  */
end_comment

begin_class
DECL|class|SpanWithinQueryBuilder
specifier|public
class|class
name|SpanWithinQueryBuilder
extends|extends
name|AbstractQueryBuilder
argument_list|<
name|SpanWithinQueryBuilder
argument_list|>
implements|implements
name|SpanQueryBuilder
argument_list|<
name|SpanWithinQueryBuilder
argument_list|>
block|{
DECL|field|NAME
specifier|public
specifier|static
specifier|final
name|String
name|NAME
init|=
literal|"span_within"
decl_stmt|;
DECL|field|big
specifier|private
specifier|final
name|SpanQueryBuilder
name|big
decl_stmt|;
DECL|field|little
specifier|private
specifier|final
name|SpanQueryBuilder
name|little
decl_stmt|;
DECL|field|PROTOTYPE
specifier|static
specifier|final
name|SpanWithinQueryBuilder
name|PROTOTYPE
init|=
operator|new
name|SpanWithinQueryBuilder
argument_list|(
literal|null
argument_list|,
literal|null
argument_list|)
decl_stmt|;
comment|/**      * Query that returns spans from<code>little</code> that are contained in a spans from<code>big</code>.      * @param big clause that must enclose {@code little} for a match.      * @param little the little clause, it must be contained within {@code big} for a match.      */
DECL|method|SpanWithinQueryBuilder
specifier|public
name|SpanWithinQueryBuilder
parameter_list|(
name|SpanQueryBuilder
name|big
parameter_list|,
name|SpanQueryBuilder
name|little
parameter_list|)
block|{
name|this
operator|.
name|little
operator|=
name|little
expr_stmt|;
name|this
operator|.
name|big
operator|=
name|big
expr_stmt|;
block|}
comment|/**      * @return the little clause, contained within {@code big} for a match.      */
DECL|method|little
specifier|public
name|SpanQueryBuilder
name|little
parameter_list|()
block|{
return|return
name|this
operator|.
name|little
return|;
block|}
comment|/**      * @return the big clause that must enclose {@code little} for a match.      */
DECL|method|big
specifier|public
name|SpanQueryBuilder
name|big
parameter_list|()
block|{
return|return
name|this
operator|.
name|big
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
DECL|method|doToQuery
specifier|protected
name|Query
name|doToQuery
parameter_list|(
name|QueryShardContext
name|context
parameter_list|)
throws|throws
name|IOException
block|{
name|Query
name|innerBig
init|=
name|big
operator|.
name|toQuery
argument_list|(
name|context
argument_list|)
decl_stmt|;
assert|assert
name|innerBig
operator|instanceof
name|SpanQuery
assert|;
name|Query
name|innerLittle
init|=
name|little
operator|.
name|toQuery
argument_list|(
name|context
argument_list|)
decl_stmt|;
assert|assert
name|innerLittle
operator|instanceof
name|SpanQuery
assert|;
return|return
operator|new
name|SpanWithinQuery
argument_list|(
operator|(
name|SpanQuery
operator|)
name|innerBig
argument_list|,
operator|(
name|SpanQuery
operator|)
name|innerLittle
argument_list|)
return|;
block|}
annotation|@
name|Override
DECL|method|validate
specifier|public
name|QueryValidationException
name|validate
parameter_list|()
block|{
name|QueryValidationException
name|validationException
init|=
literal|null
decl_stmt|;
if|if
condition|(
name|big
operator|==
literal|null
condition|)
block|{
name|validationException
operator|=
name|addValidationError
argument_list|(
literal|"inner clause [big] cannot be null."
argument_list|,
name|validationException
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|validationException
operator|=
name|validateInnerQuery
argument_list|(
name|big
argument_list|,
name|validationException
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|little
operator|==
literal|null
condition|)
block|{
name|validationException
operator|=
name|addValidationError
argument_list|(
literal|"inner clause [little] cannot be null."
argument_list|,
name|validationException
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|validationException
operator|=
name|validateInnerQuery
argument_list|(
name|little
argument_list|,
name|validationException
argument_list|)
expr_stmt|;
block|}
return|return
name|validationException
return|;
block|}
annotation|@
name|Override
DECL|method|doReadFrom
specifier|protected
name|SpanWithinQueryBuilder
name|doReadFrom
parameter_list|(
name|StreamInput
name|in
parameter_list|)
throws|throws
name|IOException
block|{
name|SpanQueryBuilder
name|big
init|=
operator|(
name|SpanQueryBuilder
operator|)
name|in
operator|.
name|readQuery
argument_list|()
decl_stmt|;
name|SpanQueryBuilder
name|little
init|=
operator|(
name|SpanQueryBuilder
operator|)
name|in
operator|.
name|readQuery
argument_list|()
decl_stmt|;
return|return
operator|new
name|SpanWithinQueryBuilder
argument_list|(
name|big
argument_list|,
name|little
argument_list|)
return|;
block|}
annotation|@
name|Override
DECL|method|doWriteTo
specifier|protected
name|void
name|doWriteTo
parameter_list|(
name|StreamOutput
name|out
parameter_list|)
throws|throws
name|IOException
block|{
name|out
operator|.
name|writeQuery
argument_list|(
name|big
argument_list|)
expr_stmt|;
name|out
operator|.
name|writeQuery
argument_list|(
name|little
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
DECL|method|doHashCode
specifier|protected
name|int
name|doHashCode
parameter_list|()
block|{
return|return
name|Objects
operator|.
name|hash
argument_list|(
name|big
argument_list|,
name|little
argument_list|)
return|;
block|}
annotation|@
name|Override
DECL|method|doEquals
specifier|protected
name|boolean
name|doEquals
parameter_list|(
name|SpanWithinQueryBuilder
name|other
parameter_list|)
block|{
return|return
name|Objects
operator|.
name|equals
argument_list|(
name|big
argument_list|,
name|other
operator|.
name|big
argument_list|)
operator|&&
name|Objects
operator|.
name|equals
argument_list|(
name|little
argument_list|,
name|other
operator|.
name|little
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

