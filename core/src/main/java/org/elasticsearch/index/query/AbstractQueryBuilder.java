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
name|action
operator|.
name|support
operator|.
name|ToXContentToBytes
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
name|common
operator|.
name|xcontent
operator|.
name|XContentBuilder
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
name|XContentType
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
name|ArrayList
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|Collection
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|List
import|;
end_import

begin_comment
comment|/**  * Base class for all classes producing lucene queries.  * Supports conversion to BytesReference and creation of lucene Query objects.  */
end_comment

begin_class
DECL|class|AbstractQueryBuilder
specifier|public
specifier|abstract
class|class
name|AbstractQueryBuilder
parameter_list|<
name|QB
extends|extends
name|QueryBuilder
parameter_list|>
extends|extends
name|ToXContentToBytes
implements|implements
name|QueryBuilder
argument_list|<
name|QB
argument_list|>
block|{
DECL|method|AbstractQueryBuilder
specifier|protected
name|AbstractQueryBuilder
parameter_list|()
block|{
name|super
argument_list|(
name|XContentType
operator|.
name|JSON
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
DECL|method|toXContent
specifier|public
name|XContentBuilder
name|toXContent
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
argument_list|()
expr_stmt|;
name|doXContent
argument_list|(
name|builder
argument_list|,
name|params
argument_list|)
expr_stmt|;
name|builder
operator|.
name|endObject
argument_list|()
expr_stmt|;
return|return
name|builder
return|;
block|}
DECL|method|doXContent
specifier|protected
specifier|abstract
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
function_decl|;
annotation|@
name|Override
comment|//norelease to be made abstract once all query builders override toQuery providing their own specific implementation.
DECL|method|toQuery
specifier|public
name|Query
name|toQuery
parameter_list|(
name|QueryParseContext
name|parseContext
parameter_list|)
throws|throws
name|QueryParsingException
throws|,
name|IOException
block|{
return|return
name|parseContext
operator|.
name|indexQueryParserService
argument_list|()
operator|.
name|queryParser
argument_list|(
name|getName
argument_list|()
argument_list|)
operator|.
name|parse
argument_list|(
name|parseContext
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
comment|// default impl does not validate, subclasses should override.
comment|//norelease to be possibly made abstract once all queries support validation
return|return
literal|null
return|;
block|}
comment|//norelease remove this once all builders implement readFrom themselves
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
return|return
literal|null
return|;
block|}
comment|//norelease remove this once all builders implement writeTo themselves
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
block|{     }
annotation|@
name|Override
DECL|method|equals
specifier|public
specifier|final
name|boolean
name|equals
parameter_list|(
name|Object
name|obj
parameter_list|)
block|{
if|if
condition|(
name|this
operator|==
name|obj
condition|)
block|{
return|return
literal|true
return|;
block|}
if|if
condition|(
name|obj
operator|==
literal|null
operator|||
name|getClass
argument_list|()
operator|!=
name|obj
operator|.
name|getClass
argument_list|()
condition|)
block|{
return|return
literal|false
return|;
block|}
annotation|@
name|SuppressWarnings
argument_list|(
literal|"unchecked"
argument_list|)
name|QB
name|other
init|=
operator|(
name|QB
operator|)
name|obj
decl_stmt|;
return|return
name|doEquals
argument_list|(
name|other
argument_list|)
return|;
block|}
comment|/**      * Indicates whether some other {@link QueryBuilder} object of the same type is "equal to" this one.      */
comment|//norelease to be made abstract once all queries are refactored
DECL|method|doEquals
specifier|protected
name|boolean
name|doEquals
parameter_list|(
name|QB
name|other
parameter_list|)
block|{
throw|throw
operator|new
name|UnsupportedOperationException
argument_list|()
throw|;
block|}
comment|/**      * This helper method checks if the object passed in is a string, if so it      * converts it to a {@link BytesRef}.      * @param obj the input object      * @return the same input object or a {@link BytesRef} representation if input was of type string      */
DECL|method|convertToBytesRefIfString
specifier|protected
specifier|static
name|Object
name|convertToBytesRefIfString
parameter_list|(
name|Object
name|obj
parameter_list|)
block|{
if|if
condition|(
name|obj
operator|instanceof
name|String
condition|)
block|{
return|return
name|BytesRefs
operator|.
name|toBytesRef
argument_list|(
name|obj
argument_list|)
return|;
block|}
return|return
name|obj
return|;
block|}
comment|/**      * This helper method checks if the object passed in is a {@link BytesRef}, if so it      * converts it to a utf8 string.      * @param obj the input object      * @return the same input object or a utf8 string if input was of type {@link BytesRef}      */
DECL|method|convertToStringIfBytesRef
specifier|protected
specifier|static
name|Object
name|convertToStringIfBytesRef
parameter_list|(
name|Object
name|obj
parameter_list|)
block|{
if|if
condition|(
name|obj
operator|instanceof
name|BytesRef
condition|)
block|{
return|return
operator|(
operator|(
name|BytesRef
operator|)
name|obj
operator|)
operator|.
name|utf8ToString
argument_list|()
return|;
block|}
return|return
name|obj
return|;
block|}
comment|/**      * Helper method to convert collection of {@link QueryBuilder} instances to lucene      * {@link Query} instances. {@link QueryBuilder} that return<tt>null</tt> calling      * their {@link QueryBuilder#toQuery(QueryParseContext)} method are not added to the      * resulting collection.      *      * @throws IOException      * @throws QueryParsingException      */
DECL|method|toQueries
specifier|protected
specifier|static
name|Collection
argument_list|<
name|Query
argument_list|>
name|toQueries
parameter_list|(
name|Collection
argument_list|<
name|QueryBuilder
argument_list|>
name|queryBuilders
parameter_list|,
name|QueryParseContext
name|parseContext
parameter_list|)
throws|throws
name|QueryParsingException
throws|,
name|IOException
block|{
name|List
argument_list|<
name|Query
argument_list|>
name|queries
init|=
operator|new
name|ArrayList
argument_list|<>
argument_list|(
name|queryBuilders
operator|.
name|size
argument_list|()
argument_list|)
decl_stmt|;
for|for
control|(
name|QueryBuilder
name|queryBuilder
range|:
name|queryBuilders
control|)
block|{
name|Query
name|query
init|=
name|queryBuilder
operator|.
name|toQuery
argument_list|(
name|parseContext
argument_list|)
decl_stmt|;
if|if
condition|(
name|query
operator|!=
literal|null
condition|)
block|{
name|queries
operator|.
name|add
argument_list|(
name|query
argument_list|)
expr_stmt|;
block|}
block|}
return|return
name|queries
return|;
block|}
comment|/**      * Utility method that converts inner query builders to xContent and      * checks for null values, rendering out empty object in this case.      */
DECL|method|doXContentInnerBuilder
specifier|protected
specifier|static
name|void
name|doXContentInnerBuilder
parameter_list|(
name|XContentBuilder
name|xContentBuilder
parameter_list|,
name|String
name|fieldName
parameter_list|,
name|QueryBuilder
name|queryBuilder
parameter_list|,
name|Params
name|params
parameter_list|)
throws|throws
name|IOException
block|{
name|xContentBuilder
operator|.
name|field
argument_list|(
name|fieldName
argument_list|)
expr_stmt|;
if|if
condition|(
name|queryBuilder
operator|!=
literal|null
condition|)
block|{
name|queryBuilder
operator|.
name|toXContent
argument_list|(
name|xContentBuilder
argument_list|,
name|params
argument_list|)
expr_stmt|;
block|}
else|else
block|{
comment|// we output an empty object, QueryParseContext#parseInnerQueryBuilder will parse this back to `null` value
name|xContentBuilder
operator|.
name|startObject
argument_list|()
expr_stmt|;
name|xContentBuilder
operator|.
name|endObject
argument_list|()
expr_stmt|;
block|}
block|}
block|}
end_class

end_unit
