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
name|unit
operator|.
name|Fuzziness
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
comment|/**  * A Query that does fuzzy matching for a specific value.  */
end_comment

begin_class
DECL|class|FuzzyQueryBuilder
specifier|public
class|class
name|FuzzyQueryBuilder
extends|extends
name|AbstractQueryBuilder
argument_list|<
name|FuzzyQueryBuilder
argument_list|>
implements|implements
name|MultiTermQueryBuilder
argument_list|<
name|FuzzyQueryBuilder
argument_list|>
block|{
DECL|field|NAME
specifier|public
specifier|static
specifier|final
name|String
name|NAME
init|=
literal|"fuzzy"
decl_stmt|;
DECL|field|name
specifier|private
specifier|final
name|String
name|name
decl_stmt|;
DECL|field|value
specifier|private
specifier|final
name|Object
name|value
decl_stmt|;
DECL|field|fuzziness
specifier|private
name|Fuzziness
name|fuzziness
decl_stmt|;
DECL|field|prefixLength
specifier|private
name|Integer
name|prefixLength
decl_stmt|;
DECL|field|maxExpansions
specifier|private
name|Integer
name|maxExpansions
decl_stmt|;
comment|//LUCENE 4 UPGRADE  we need a testcase for this + documentation
DECL|field|transpositions
specifier|private
name|Boolean
name|transpositions
decl_stmt|;
DECL|field|rewrite
specifier|private
name|String
name|rewrite
decl_stmt|;
DECL|field|PROTOTYPE
specifier|static
specifier|final
name|FuzzyQueryBuilder
name|PROTOTYPE
init|=
operator|new
name|FuzzyQueryBuilder
argument_list|(
literal|null
argument_list|,
literal|null
argument_list|)
decl_stmt|;
comment|/**      * Constructs a new fuzzy query.      *      * @param name  The name of the field      * @param value The value of the text      */
DECL|method|FuzzyQueryBuilder
specifier|public
name|FuzzyQueryBuilder
parameter_list|(
name|String
name|name
parameter_list|,
name|Object
name|value
parameter_list|)
block|{
name|this
operator|.
name|name
operator|=
name|name
expr_stmt|;
name|this
operator|.
name|value
operator|=
name|value
expr_stmt|;
block|}
comment|/**      * Constructs a new fuzzy query.      *      * @param name  The name of the field      * @param value The value of the text      */
DECL|method|FuzzyQueryBuilder
specifier|public
name|FuzzyQueryBuilder
parameter_list|(
name|String
name|name
parameter_list|,
name|String
name|value
parameter_list|)
block|{
name|this
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
comment|/**      * Constructs a new fuzzy query.      *      * @param name  The name of the field      * @param value The value of the text      */
DECL|method|FuzzyQueryBuilder
specifier|public
name|FuzzyQueryBuilder
parameter_list|(
name|String
name|name
parameter_list|,
name|int
name|value
parameter_list|)
block|{
name|this
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
comment|/**      * Constructs a new fuzzy query.      *      * @param name  The name of the field      * @param value The value of the text      */
DECL|method|FuzzyQueryBuilder
specifier|public
name|FuzzyQueryBuilder
parameter_list|(
name|String
name|name
parameter_list|,
name|long
name|value
parameter_list|)
block|{
name|this
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
comment|/**      * Constructs a new fuzzy query.      *      * @param name  The name of the field      * @param value The value of the text      */
DECL|method|FuzzyQueryBuilder
specifier|public
name|FuzzyQueryBuilder
parameter_list|(
name|String
name|name
parameter_list|,
name|float
name|value
parameter_list|)
block|{
name|this
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
comment|/**      * Constructs a new fuzzy query.      *      * @param name  The name of the field      * @param value The value of the text      */
DECL|method|FuzzyQueryBuilder
specifier|public
name|FuzzyQueryBuilder
parameter_list|(
name|String
name|name
parameter_list|,
name|double
name|value
parameter_list|)
block|{
name|this
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
comment|// NO COMMIT: not sure we should also allow boolean?
comment|/**      * Constructs a new fuzzy query.      *      * @param name  The name of the field      * @param value The value of the text      */
DECL|method|FuzzyQueryBuilder
specifier|public
name|FuzzyQueryBuilder
parameter_list|(
name|String
name|name
parameter_list|,
name|boolean
name|value
parameter_list|)
block|{
name|this
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
DECL|method|fuzziness
specifier|public
name|FuzzyQueryBuilder
name|fuzziness
parameter_list|(
name|Fuzziness
name|fuzziness
parameter_list|)
block|{
name|this
operator|.
name|fuzziness
operator|=
name|fuzziness
expr_stmt|;
return|return
name|this
return|;
block|}
DECL|method|prefixLength
specifier|public
name|FuzzyQueryBuilder
name|prefixLength
parameter_list|(
name|int
name|prefixLength
parameter_list|)
block|{
name|this
operator|.
name|prefixLength
operator|=
name|prefixLength
expr_stmt|;
return|return
name|this
return|;
block|}
DECL|method|maxExpansions
specifier|public
name|FuzzyQueryBuilder
name|maxExpansions
parameter_list|(
name|int
name|maxExpansions
parameter_list|)
block|{
name|this
operator|.
name|maxExpansions
operator|=
name|maxExpansions
expr_stmt|;
return|return
name|this
return|;
block|}
DECL|method|transpositions
specifier|public
name|FuzzyQueryBuilder
name|transpositions
parameter_list|(
name|boolean
name|transpositions
parameter_list|)
block|{
name|this
operator|.
name|transpositions
operator|=
name|transpositions
expr_stmt|;
return|return
name|this
return|;
block|}
DECL|method|rewrite
specifier|public
name|FuzzyQueryBuilder
name|rewrite
parameter_list|(
name|String
name|rewrite
parameter_list|)
block|{
name|this
operator|.
name|rewrite
operator|=
name|rewrite
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
name|startObject
argument_list|(
name|name
argument_list|)
expr_stmt|;
name|builder
operator|.
name|field
argument_list|(
literal|"value"
argument_list|,
name|value
argument_list|)
expr_stmt|;
if|if
condition|(
name|transpositions
operator|!=
literal|null
condition|)
block|{
name|builder
operator|.
name|field
argument_list|(
literal|"transpositions"
argument_list|,
name|transpositions
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|fuzziness
operator|!=
literal|null
condition|)
block|{
name|fuzziness
operator|.
name|toXContent
argument_list|(
name|builder
argument_list|,
name|params
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|prefixLength
operator|!=
literal|null
condition|)
block|{
name|builder
operator|.
name|field
argument_list|(
literal|"prefix_length"
argument_list|,
name|prefixLength
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|maxExpansions
operator|!=
literal|null
condition|)
block|{
name|builder
operator|.
name|field
argument_list|(
literal|"max_expansions"
argument_list|,
name|maxExpansions
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|rewrite
operator|!=
literal|null
condition|)
block|{
name|builder
operator|.
name|field
argument_list|(
literal|"rewrite"
argument_list|,
name|rewrite
argument_list|)
expr_stmt|;
block|}
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
name|builder
operator|.
name|endObject
argument_list|()
expr_stmt|;
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

