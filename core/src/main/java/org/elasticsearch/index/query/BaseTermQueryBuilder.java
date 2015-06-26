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

begin_class
DECL|class|BaseTermQueryBuilder
specifier|public
specifier|abstract
class|class
name|BaseTermQueryBuilder
parameter_list|<
name|QB
extends|extends
name|BaseTermQueryBuilder
parameter_list|<
name|QB
parameter_list|>
parameter_list|>
extends|extends
name|AbstractQueryBuilder
argument_list|<
name|QB
argument_list|>
implements|implements
name|BoostableQueryBuilder
argument_list|<
name|QB
argument_list|>
block|{
comment|/** Name of field to match against. */
DECL|field|fieldName
specifier|protected
specifier|final
name|String
name|fieldName
decl_stmt|;
comment|/** Value to find matches for. */
DECL|field|value
specifier|protected
specifier|final
name|Object
name|value
decl_stmt|;
comment|/** Query boost. */
DECL|field|boost
specifier|protected
name|float
name|boost
init|=
literal|1.0f
decl_stmt|;
comment|/** Name of the query. */
DECL|field|queryName
specifier|protected
name|String
name|queryName
decl_stmt|;
comment|/**      * Constructs a new base term query.      *      * @param fieldName  The name of the field      * @param value The value of the term      */
DECL|method|BaseTermQueryBuilder
specifier|public
name|BaseTermQueryBuilder
parameter_list|(
name|String
name|fieldName
parameter_list|,
name|String
name|value
parameter_list|)
block|{
name|this
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
comment|/**      * Constructs a new base term query.      *      * @param fieldName  The name of the field      * @param value The value of the term      */
DECL|method|BaseTermQueryBuilder
specifier|public
name|BaseTermQueryBuilder
parameter_list|(
name|String
name|fieldName
parameter_list|,
name|int
name|value
parameter_list|)
block|{
name|this
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
comment|/**      * Constructs a new base term query.      *      * @param fieldName  The name of the field      * @param value The value of the term      */
DECL|method|BaseTermQueryBuilder
specifier|public
name|BaseTermQueryBuilder
parameter_list|(
name|String
name|fieldName
parameter_list|,
name|long
name|value
parameter_list|)
block|{
name|this
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
comment|/**      * Constructs a new base term query.      *      * @param fieldName  The name of the field      * @param value The value of the term      */
DECL|method|BaseTermQueryBuilder
specifier|public
name|BaseTermQueryBuilder
parameter_list|(
name|String
name|fieldName
parameter_list|,
name|float
name|value
parameter_list|)
block|{
name|this
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
comment|/**      * Constructs a new base term query.      *      * @param fieldName  The name of the field      * @param value The value of the term      */
DECL|method|BaseTermQueryBuilder
specifier|public
name|BaseTermQueryBuilder
parameter_list|(
name|String
name|fieldName
parameter_list|,
name|double
name|value
parameter_list|)
block|{
name|this
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
comment|/**      * Constructs a new base term query.      *      * @param fieldName  The name of the field      * @param value The value of the term      */
DECL|method|BaseTermQueryBuilder
specifier|public
name|BaseTermQueryBuilder
parameter_list|(
name|String
name|fieldName
parameter_list|,
name|boolean
name|value
parameter_list|)
block|{
name|this
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
comment|/**      * Constructs a new base term query.      * In case value is assigned to a string, we internally convert it to a {@link BytesRef}      * because in {@link TermQueryParser} and {@link SpanTermQueryParser} string values are parsed to {@link BytesRef}      * and we want internal representation of query to be equal regardless of whether it was created from XContent or via Java API.      *      * @param fieldName  The name of the field      * @param value The value of the term      */
DECL|method|BaseTermQueryBuilder
specifier|public
name|BaseTermQueryBuilder
parameter_list|(
name|String
name|fieldName
parameter_list|,
name|Object
name|value
parameter_list|)
block|{
name|this
operator|.
name|fieldName
operator|=
name|fieldName
expr_stmt|;
name|this
operator|.
name|value
operator|=
name|convertToBytesRefIfString
argument_list|(
name|value
argument_list|)
expr_stmt|;
block|}
comment|/** Returns the field name used in this query. */
DECL|method|fieldName
specifier|public
name|String
name|fieldName
parameter_list|()
block|{
return|return
name|this
operator|.
name|fieldName
return|;
block|}
comment|/**      *  Returns the value used in this query.      *  If necessary, converts internal {@link BytesRef} representation back to string.      */
DECL|method|value
specifier|public
name|Object
name|value
parameter_list|()
block|{
return|return
name|convertToStringIfBytesRef
argument_list|(
name|this
operator|.
name|value
argument_list|)
return|;
block|}
comment|/** Returns the query name for the query. */
DECL|method|queryName
specifier|public
name|String
name|queryName
parameter_list|()
block|{
return|return
name|this
operator|.
name|queryName
return|;
block|}
comment|/**      * Sets the query name for the query.      */
annotation|@
name|SuppressWarnings
argument_list|(
literal|"unchecked"
argument_list|)
DECL|method|queryName
specifier|public
name|QB
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
operator|(
name|QB
operator|)
name|this
return|;
block|}
comment|/** Returns the boost for this query. */
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
comment|/**      * Sets the boost for this query.  Documents matching this query will (in addition to the normal      * weightings) have their score multiplied by the boost provided.      */
annotation|@
name|SuppressWarnings
argument_list|(
literal|"unchecked"
argument_list|)
annotation|@
name|Override
DECL|method|boost
specifier|public
name|QB
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
operator|(
name|QB
operator|)
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
name|builder
operator|.
name|startObject
argument_list|(
name|getName
argument_list|()
argument_list|)
expr_stmt|;
if|if
condition|(
name|boost
operator|==
literal|1.0f
operator|&&
name|queryName
operator|==
literal|null
condition|)
block|{
name|builder
operator|.
name|field
argument_list|(
name|fieldName
argument_list|,
name|convertToStringIfBytesRef
argument_list|(
name|this
operator|.
name|value
argument_list|)
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|builder
operator|.
name|startObject
argument_list|(
name|fieldName
argument_list|)
expr_stmt|;
name|builder
operator|.
name|field
argument_list|(
literal|"value"
argument_list|,
name|convertToStringIfBytesRef
argument_list|(
name|this
operator|.
name|value
argument_list|)
argument_list|)
expr_stmt|;
name|builder
operator|.
name|field
argument_list|(
literal|"boost"
argument_list|,
name|boost
argument_list|)
expr_stmt|;
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
name|builder
operator|.
name|endObject
argument_list|()
expr_stmt|;
block|}
comment|/** Returns a {@link QueryValidationException} if fieldName is null or empty, or if value is null. */
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
name|fieldName
operator|==
literal|null
operator|||
name|fieldName
operator|.
name|isEmpty
argument_list|()
condition|)
block|{
name|validationException
operator|=
name|addValidationError
argument_list|(
literal|"field name cannot be null or empty."
argument_list|,
name|validationException
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|value
operator|==
literal|null
condition|)
block|{
name|validationException
operator|=
name|addValidationError
argument_list|(
literal|"value cannot be null."
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
DECL|method|hashCode
specifier|public
name|int
name|hashCode
parameter_list|()
block|{
return|return
name|Objects
operator|.
name|hash
argument_list|(
name|getClass
argument_list|()
argument_list|,
name|fieldName
argument_list|,
name|value
argument_list|,
name|boost
argument_list|,
name|queryName
argument_list|)
return|;
block|}
annotation|@
name|Override
DECL|method|doEquals
specifier|public
specifier|final
name|boolean
name|doEquals
parameter_list|(
name|BaseTermQueryBuilder
name|other
parameter_list|)
block|{
return|return
name|Objects
operator|.
name|equals
argument_list|(
name|fieldName
argument_list|,
name|other
operator|.
name|fieldName
argument_list|)
operator|&&
name|Objects
operator|.
name|equals
argument_list|(
name|value
argument_list|,
name|other
operator|.
name|value
argument_list|)
operator|&&
name|Objects
operator|.
name|equals
argument_list|(
name|boost
argument_list|,
name|other
operator|.
name|boost
argument_list|)
operator|&&
name|Objects
operator|.
name|equals
argument_list|(
name|queryName
argument_list|,
name|other
operator|.
name|queryName
argument_list|)
return|;
block|}
annotation|@
name|Override
DECL|method|readFrom
specifier|public
name|QB
name|readFrom
parameter_list|(
name|StreamInput
name|in
parameter_list|)
throws|throws
name|IOException
block|{
name|QB
name|emptyBuilder
init|=
name|createBuilder
argument_list|(
name|in
operator|.
name|readString
argument_list|()
argument_list|,
name|in
operator|.
name|readGenericValue
argument_list|()
argument_list|)
decl_stmt|;
name|emptyBuilder
operator|.
name|boost
operator|=
name|in
operator|.
name|readFloat
argument_list|()
expr_stmt|;
name|emptyBuilder
operator|.
name|queryName
operator|=
name|in
operator|.
name|readOptionalString
argument_list|()
expr_stmt|;
return|return
name|emptyBuilder
return|;
block|}
DECL|method|createBuilder
specifier|protected
specifier|abstract
name|QB
name|createBuilder
parameter_list|(
name|String
name|fieldName
parameter_list|,
name|Object
name|value
parameter_list|)
function_decl|;
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
name|writeString
argument_list|(
name|fieldName
argument_list|)
expr_stmt|;
name|out
operator|.
name|writeGenericValue
argument_list|(
name|value
argument_list|)
expr_stmt|;
name|out
operator|.
name|writeFloat
argument_list|(
name|boost
argument_list|)
expr_stmt|;
name|out
operator|.
name|writeOptionalString
argument_list|(
name|queryName
argument_list|)
expr_stmt|;
block|}
block|}
end_class

end_unit

