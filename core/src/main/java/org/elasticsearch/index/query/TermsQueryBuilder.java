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
name|queries
operator|.
name|TermsQuery
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
name|BooleanClause
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
name|BooleanQuery
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
name|get
operator|.
name|GetRequest
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
name|get
operator|.
name|GetResponse
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|client
operator|.
name|Client
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
name|ParseField
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
name|ParsingException
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
name|Strings
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
name|org
operator|.
name|elasticsearch
operator|.
name|common
operator|.
name|xcontent
operator|.
name|XContentParser
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
name|support
operator|.
name|XContentMapValues
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
name|org
operator|.
name|elasticsearch
operator|.
name|indices
operator|.
name|TermsLookup
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
name|Arrays
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

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|Objects
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|stream
operator|.
name|Collectors
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|stream
operator|.
name|IntStream
import|;
end_import

begin_comment
comment|/**  * A filter for a field based on several terms matching on any of them.  */
end_comment

begin_class
DECL|class|TermsQueryBuilder
specifier|public
class|class
name|TermsQueryBuilder
extends|extends
name|AbstractQueryBuilder
argument_list|<
name|TermsQueryBuilder
argument_list|>
block|{
DECL|field|NAME
specifier|public
specifier|static
specifier|final
name|String
name|NAME
init|=
literal|"terms"
decl_stmt|;
DECL|field|QUERY_NAME_FIELD
specifier|public
specifier|static
specifier|final
name|ParseField
name|QUERY_NAME_FIELD
init|=
operator|new
name|ParseField
argument_list|(
name|NAME
argument_list|,
literal|"in"
argument_list|)
decl_stmt|;
DECL|field|fieldName
specifier|private
specifier|final
name|String
name|fieldName
decl_stmt|;
DECL|field|values
specifier|private
specifier|final
name|List
argument_list|<
name|?
argument_list|>
name|values
decl_stmt|;
DECL|field|termsLookup
specifier|private
specifier|final
name|TermsLookup
name|termsLookup
decl_stmt|;
DECL|method|TermsQueryBuilder
specifier|public
name|TermsQueryBuilder
parameter_list|(
name|String
name|fieldName
parameter_list|,
name|TermsLookup
name|termsLookup
parameter_list|)
block|{
name|this
argument_list|(
name|fieldName
argument_list|,
literal|null
argument_list|,
name|termsLookup
argument_list|)
expr_stmt|;
block|}
comment|/**      * constructor used internally for serialization of both value / termslookup variants      */
DECL|method|TermsQueryBuilder
name|TermsQueryBuilder
parameter_list|(
name|String
name|fieldName
parameter_list|,
name|List
argument_list|<
name|Object
argument_list|>
name|values
parameter_list|,
name|TermsLookup
name|termsLookup
parameter_list|)
block|{
if|if
condition|(
name|Strings
operator|.
name|isEmpty
argument_list|(
name|fieldName
argument_list|)
condition|)
block|{
throw|throw
operator|new
name|IllegalArgumentException
argument_list|(
literal|"field name cannot be null."
argument_list|)
throw|;
block|}
if|if
condition|(
name|values
operator|==
literal|null
operator|&&
name|termsLookup
operator|==
literal|null
condition|)
block|{
throw|throw
operator|new
name|IllegalArgumentException
argument_list|(
literal|"No value or termsLookup specified for terms query"
argument_list|)
throw|;
block|}
if|if
condition|(
name|values
operator|!=
literal|null
operator|&&
name|termsLookup
operator|!=
literal|null
condition|)
block|{
throw|throw
operator|new
name|IllegalArgumentException
argument_list|(
literal|"Both values and termsLookup specified for terms query"
argument_list|)
throw|;
block|}
name|this
operator|.
name|fieldName
operator|=
name|fieldName
expr_stmt|;
name|this
operator|.
name|values
operator|=
name|values
expr_stmt|;
name|this
operator|.
name|termsLookup
operator|=
name|termsLookup
expr_stmt|;
block|}
comment|/**      * A filter for a field based on several terms matching on any of them.      *      * @param fieldName The field name      * @param values The terms      */
DECL|method|TermsQueryBuilder
specifier|public
name|TermsQueryBuilder
parameter_list|(
name|String
name|fieldName
parameter_list|,
name|String
modifier|...
name|values
parameter_list|)
block|{
name|this
argument_list|(
name|fieldName
argument_list|,
name|values
operator|!=
literal|null
condition|?
name|Arrays
operator|.
name|asList
argument_list|(
name|values
argument_list|)
else|:
literal|null
argument_list|)
expr_stmt|;
block|}
comment|/**      * A filter for a field based on several terms matching on any of them.      *      * @param fieldName The field name      * @param values The terms      */
DECL|method|TermsQueryBuilder
specifier|public
name|TermsQueryBuilder
parameter_list|(
name|String
name|fieldName
parameter_list|,
name|int
modifier|...
name|values
parameter_list|)
block|{
name|this
argument_list|(
name|fieldName
argument_list|,
name|values
operator|!=
literal|null
condition|?
name|Arrays
operator|.
name|stream
argument_list|(
name|values
argument_list|)
operator|.
name|mapToObj
argument_list|(
name|s
lambda|->
name|s
argument_list|)
operator|.
name|collect
argument_list|(
name|Collectors
operator|.
name|toList
argument_list|()
argument_list|)
else|:
operator|(
name|Iterable
argument_list|<
name|?
argument_list|>
operator|)
literal|null
argument_list|)
expr_stmt|;
block|}
comment|/**      * A filter for a field based on several terms matching on any of them.      *      * @param fieldName The field name      * @param values The terms      */
DECL|method|TermsQueryBuilder
specifier|public
name|TermsQueryBuilder
parameter_list|(
name|String
name|fieldName
parameter_list|,
name|long
modifier|...
name|values
parameter_list|)
block|{
name|this
argument_list|(
name|fieldName
argument_list|,
name|values
operator|!=
literal|null
condition|?
name|Arrays
operator|.
name|stream
argument_list|(
name|values
argument_list|)
operator|.
name|mapToObj
argument_list|(
name|s
lambda|->
name|s
argument_list|)
operator|.
name|collect
argument_list|(
name|Collectors
operator|.
name|toList
argument_list|()
argument_list|)
else|:
operator|(
name|Iterable
argument_list|<
name|?
argument_list|>
operator|)
literal|null
argument_list|)
expr_stmt|;
block|}
comment|/**      * A filter for a field based on several terms matching on any of them.      *      * @param fieldName The field name      * @param values The terms      */
DECL|method|TermsQueryBuilder
specifier|public
name|TermsQueryBuilder
parameter_list|(
name|String
name|fieldName
parameter_list|,
name|float
modifier|...
name|values
parameter_list|)
block|{
name|this
argument_list|(
name|fieldName
argument_list|,
name|values
operator|!=
literal|null
condition|?
name|IntStream
operator|.
name|range
argument_list|(
literal|0
argument_list|,
name|values
operator|.
name|length
argument_list|)
operator|.
name|mapToObj
argument_list|(
name|i
lambda|->
name|values
index|[
name|i
index|]
argument_list|)
operator|.
name|collect
argument_list|(
name|Collectors
operator|.
name|toList
argument_list|()
argument_list|)
else|:
operator|(
name|Iterable
argument_list|<
name|?
argument_list|>
operator|)
literal|null
argument_list|)
expr_stmt|;
block|}
comment|/**      * A filter for a field based on several terms matching on any of them.      *      * @param fieldName The field name      * @param values The terms      */
DECL|method|TermsQueryBuilder
specifier|public
name|TermsQueryBuilder
parameter_list|(
name|String
name|fieldName
parameter_list|,
name|double
modifier|...
name|values
parameter_list|)
block|{
name|this
argument_list|(
name|fieldName
argument_list|,
name|values
operator|!=
literal|null
condition|?
name|Arrays
operator|.
name|stream
argument_list|(
name|values
argument_list|)
operator|.
name|mapToObj
argument_list|(
name|s
lambda|->
name|s
argument_list|)
operator|.
name|collect
argument_list|(
name|Collectors
operator|.
name|toList
argument_list|()
argument_list|)
else|:
operator|(
name|Iterable
argument_list|<
name|?
argument_list|>
operator|)
literal|null
argument_list|)
expr_stmt|;
block|}
comment|/**      * A filter for a field based on several terms matching on any of them.      *      * @param fieldName The field name      * @param values The terms      */
DECL|method|TermsQueryBuilder
specifier|public
name|TermsQueryBuilder
parameter_list|(
name|String
name|fieldName
parameter_list|,
name|Object
modifier|...
name|values
parameter_list|)
block|{
name|this
argument_list|(
name|fieldName
argument_list|,
name|values
operator|!=
literal|null
condition|?
name|Arrays
operator|.
name|asList
argument_list|(
name|values
argument_list|)
else|:
operator|(
name|Iterable
argument_list|<
name|?
argument_list|>
operator|)
literal|null
argument_list|)
expr_stmt|;
block|}
comment|/**      * A filter for a field based on several terms matching on any of them.      *      * @param fieldName The field name      * @param values The terms      */
DECL|method|TermsQueryBuilder
specifier|public
name|TermsQueryBuilder
parameter_list|(
name|String
name|fieldName
parameter_list|,
name|Iterable
argument_list|<
name|?
argument_list|>
name|values
parameter_list|)
block|{
if|if
condition|(
name|Strings
operator|.
name|isEmpty
argument_list|(
name|fieldName
argument_list|)
condition|)
block|{
throw|throw
operator|new
name|IllegalArgumentException
argument_list|(
literal|"field name cannot be null."
argument_list|)
throw|;
block|}
if|if
condition|(
name|values
operator|==
literal|null
condition|)
block|{
throw|throw
operator|new
name|IllegalArgumentException
argument_list|(
literal|"No value specified for terms query"
argument_list|)
throw|;
block|}
name|this
operator|.
name|fieldName
operator|=
name|fieldName
expr_stmt|;
name|this
operator|.
name|values
operator|=
name|convertToBytesRefListIfStringList
argument_list|(
name|values
argument_list|)
expr_stmt|;
name|this
operator|.
name|termsLookup
operator|=
literal|null
expr_stmt|;
block|}
comment|/**      * Read from a stream.      */
DECL|method|TermsQueryBuilder
specifier|public
name|TermsQueryBuilder
parameter_list|(
name|StreamInput
name|in
parameter_list|)
throws|throws
name|IOException
block|{
name|super
argument_list|(
name|in
argument_list|)
expr_stmt|;
name|fieldName
operator|=
name|in
operator|.
name|readString
argument_list|()
expr_stmt|;
name|termsLookup
operator|=
name|in
operator|.
name|readOptionalWriteable
argument_list|(
name|TermsLookup
operator|::
operator|new
argument_list|)
expr_stmt|;
name|values
operator|=
operator|(
name|List
argument_list|<
name|?
argument_list|>
operator|)
name|in
operator|.
name|readGenericValue
argument_list|()
expr_stmt|;
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
name|writeString
argument_list|(
name|fieldName
argument_list|)
expr_stmt|;
name|out
operator|.
name|writeOptionalWriteable
argument_list|(
name|termsLookup
argument_list|)
expr_stmt|;
name|out
operator|.
name|writeGenericValue
argument_list|(
name|values
argument_list|)
expr_stmt|;
block|}
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
DECL|method|values
specifier|public
name|List
argument_list|<
name|Object
argument_list|>
name|values
parameter_list|()
block|{
return|return
name|convertToStringListIfBytesRefList
argument_list|(
name|this
operator|.
name|values
argument_list|)
return|;
block|}
DECL|method|termsLookup
specifier|public
name|TermsLookup
name|termsLookup
parameter_list|()
block|{
return|return
name|this
operator|.
name|termsLookup
return|;
block|}
comment|/**      * Same as {@link #convertToBytesRefIfString} but on Iterable.      * @param objs the Iterable of input object      * @return the same input or a list of {@link BytesRef} representation if input was a list of type string      */
DECL|method|convertToBytesRefListIfStringList
specifier|private
specifier|static
name|List
argument_list|<
name|Object
argument_list|>
name|convertToBytesRefListIfStringList
parameter_list|(
name|Iterable
argument_list|<
name|?
argument_list|>
name|objs
parameter_list|)
block|{
if|if
condition|(
name|objs
operator|==
literal|null
condition|)
block|{
return|return
literal|null
return|;
block|}
name|List
argument_list|<
name|Object
argument_list|>
name|newObjs
init|=
operator|new
name|ArrayList
argument_list|<>
argument_list|()
decl_stmt|;
for|for
control|(
name|Object
name|obj
range|:
name|objs
control|)
block|{
name|newObjs
operator|.
name|add
argument_list|(
name|convertToBytesRefIfString
argument_list|(
name|obj
argument_list|)
argument_list|)
expr_stmt|;
block|}
return|return
name|newObjs
return|;
block|}
comment|/**      * Same as {@link #convertToStringIfBytesRef} but on Iterable.      * @param objs the Iterable of input object      * @return the same input or a list of utf8 string if input was a list of type {@link BytesRef}      */
DECL|method|convertToStringListIfBytesRefList
specifier|private
specifier|static
name|List
argument_list|<
name|Object
argument_list|>
name|convertToStringListIfBytesRefList
parameter_list|(
name|Iterable
argument_list|<
name|?
argument_list|>
name|objs
parameter_list|)
block|{
if|if
condition|(
name|objs
operator|==
literal|null
condition|)
block|{
return|return
literal|null
return|;
block|}
name|List
argument_list|<
name|Object
argument_list|>
name|newObjs
init|=
operator|new
name|ArrayList
argument_list|<>
argument_list|()
decl_stmt|;
for|for
control|(
name|Object
name|obj
range|:
name|objs
control|)
block|{
name|newObjs
operator|.
name|add
argument_list|(
name|convertToStringIfBytesRef
argument_list|(
name|obj
argument_list|)
argument_list|)
expr_stmt|;
block|}
return|return
name|newObjs
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
if|if
condition|(
name|this
operator|.
name|termsLookup
operator|!=
literal|null
condition|)
block|{
name|builder
operator|.
name|startObject
argument_list|(
name|fieldName
argument_list|)
expr_stmt|;
name|termsLookup
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
name|endObject
argument_list|()
expr_stmt|;
block|}
else|else
block|{
name|builder
operator|.
name|field
argument_list|(
name|fieldName
argument_list|,
name|convertToStringListIfBytesRefList
argument_list|(
name|values
argument_list|)
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
block|}
DECL|method|fromXContent
specifier|public
specifier|static
name|TermsQueryBuilder
name|fromXContent
parameter_list|(
name|QueryParseContext
name|parseContext
parameter_list|)
throws|throws
name|IOException
block|{
name|XContentParser
name|parser
init|=
name|parseContext
operator|.
name|parser
argument_list|()
decl_stmt|;
name|String
name|fieldName
init|=
literal|null
decl_stmt|;
name|List
argument_list|<
name|Object
argument_list|>
name|values
init|=
literal|null
decl_stmt|;
name|TermsLookup
name|termsLookup
init|=
literal|null
decl_stmt|;
name|String
name|queryName
init|=
literal|null
decl_stmt|;
name|float
name|boost
init|=
name|AbstractQueryBuilder
operator|.
name|DEFAULT_BOOST
decl_stmt|;
name|XContentParser
operator|.
name|Token
name|token
decl_stmt|;
name|String
name|currentFieldName
init|=
literal|null
decl_stmt|;
while|while
condition|(
operator|(
name|token
operator|=
name|parser
operator|.
name|nextToken
argument_list|()
operator|)
operator|!=
name|XContentParser
operator|.
name|Token
operator|.
name|END_OBJECT
condition|)
block|{
if|if
condition|(
name|token
operator|==
name|XContentParser
operator|.
name|Token
operator|.
name|FIELD_NAME
condition|)
block|{
name|currentFieldName
operator|=
name|parser
operator|.
name|currentName
argument_list|()
expr_stmt|;
block|}
elseif|else
if|if
condition|(
name|parseContext
operator|.
name|isDeprecatedSetting
argument_list|(
name|currentFieldName
argument_list|)
condition|)
block|{
comment|// skip
block|}
elseif|else
if|if
condition|(
name|token
operator|==
name|XContentParser
operator|.
name|Token
operator|.
name|START_ARRAY
condition|)
block|{
if|if
condition|(
name|fieldName
operator|!=
literal|null
condition|)
block|{
throw|throw
operator|new
name|ParsingException
argument_list|(
name|parser
operator|.
name|getTokenLocation
argument_list|()
argument_list|,
literal|"["
operator|+
name|TermsQueryBuilder
operator|.
name|NAME
operator|+
literal|"] query does not support multiple fields"
argument_list|)
throw|;
block|}
name|fieldName
operator|=
name|currentFieldName
expr_stmt|;
name|values
operator|=
name|parseValues
argument_list|(
name|parser
argument_list|)
expr_stmt|;
block|}
elseif|else
if|if
condition|(
name|token
operator|==
name|XContentParser
operator|.
name|Token
operator|.
name|START_OBJECT
condition|)
block|{
if|if
condition|(
name|fieldName
operator|!=
literal|null
condition|)
block|{
throw|throw
operator|new
name|ParsingException
argument_list|(
name|parser
operator|.
name|getTokenLocation
argument_list|()
argument_list|,
literal|"["
operator|+
name|TermsQueryBuilder
operator|.
name|NAME
operator|+
literal|"] query does not support more than one field. "
operator|+
literal|"Already got: ["
operator|+
name|fieldName
operator|+
literal|"] but also found ["
operator|+
name|currentFieldName
operator|+
literal|"]"
argument_list|)
throw|;
block|}
name|fieldName
operator|=
name|currentFieldName
expr_stmt|;
name|termsLookup
operator|=
name|TermsLookup
operator|.
name|parseTermsLookup
argument_list|(
name|parser
argument_list|)
expr_stmt|;
block|}
elseif|else
if|if
condition|(
name|token
operator|.
name|isValue
argument_list|()
condition|)
block|{
if|if
condition|(
name|parseContext
operator|.
name|getParseFieldMatcher
argument_list|()
operator|.
name|match
argument_list|(
name|currentFieldName
argument_list|,
name|AbstractQueryBuilder
operator|.
name|BOOST_FIELD
argument_list|)
condition|)
block|{
name|boost
operator|=
name|parser
operator|.
name|floatValue
argument_list|()
expr_stmt|;
block|}
elseif|else
if|if
condition|(
name|parseContext
operator|.
name|getParseFieldMatcher
argument_list|()
operator|.
name|match
argument_list|(
name|currentFieldName
argument_list|,
name|AbstractQueryBuilder
operator|.
name|NAME_FIELD
argument_list|)
condition|)
block|{
name|queryName
operator|=
name|parser
operator|.
name|text
argument_list|()
expr_stmt|;
block|}
else|else
block|{
throw|throw
operator|new
name|ParsingException
argument_list|(
name|parser
operator|.
name|getTokenLocation
argument_list|()
argument_list|,
literal|"["
operator|+
name|TermsQueryBuilder
operator|.
name|NAME
operator|+
literal|"] query does not support ["
operator|+
name|currentFieldName
operator|+
literal|"]"
argument_list|)
throw|;
block|}
block|}
else|else
block|{
throw|throw
operator|new
name|ParsingException
argument_list|(
name|parser
operator|.
name|getTokenLocation
argument_list|()
argument_list|,
literal|"["
operator|+
name|TermsQueryBuilder
operator|.
name|NAME
operator|+
literal|"] unknown token ["
operator|+
name|token
operator|+
literal|"] after ["
operator|+
name|currentFieldName
operator|+
literal|"]"
argument_list|)
throw|;
block|}
block|}
if|if
condition|(
name|fieldName
operator|==
literal|null
condition|)
block|{
throw|throw
operator|new
name|ParsingException
argument_list|(
name|parser
operator|.
name|getTokenLocation
argument_list|()
argument_list|,
literal|"["
operator|+
name|TermsQueryBuilder
operator|.
name|NAME
operator|+
literal|"] query requires a field name, "
operator|+
literal|"followed by array of terms or a document lookup specification"
argument_list|)
throw|;
block|}
return|return
operator|new
name|TermsQueryBuilder
argument_list|(
name|fieldName
argument_list|,
name|values
argument_list|,
name|termsLookup
argument_list|)
operator|.
name|boost
argument_list|(
name|boost
argument_list|)
operator|.
name|queryName
argument_list|(
name|queryName
argument_list|)
return|;
block|}
DECL|method|parseValues
specifier|private
specifier|static
name|List
argument_list|<
name|Object
argument_list|>
name|parseValues
parameter_list|(
name|XContentParser
name|parser
parameter_list|)
throws|throws
name|IOException
block|{
name|List
argument_list|<
name|Object
argument_list|>
name|values
init|=
operator|new
name|ArrayList
argument_list|<>
argument_list|()
decl_stmt|;
while|while
condition|(
name|parser
operator|.
name|nextToken
argument_list|()
operator|!=
name|XContentParser
operator|.
name|Token
operator|.
name|END_ARRAY
condition|)
block|{
name|Object
name|value
init|=
name|parser
operator|.
name|objectBytes
argument_list|()
decl_stmt|;
if|if
condition|(
name|value
operator|==
literal|null
condition|)
block|{
throw|throw
operator|new
name|ParsingException
argument_list|(
name|parser
operator|.
name|getTokenLocation
argument_list|()
argument_list|,
literal|"No value specified for terms query"
argument_list|)
throw|;
block|}
name|values
operator|.
name|add
argument_list|(
name|value
argument_list|)
expr_stmt|;
block|}
return|return
name|values
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
if|if
condition|(
name|termsLookup
operator|!=
literal|null
condition|)
block|{
throw|throw
operator|new
name|UnsupportedOperationException
argument_list|(
literal|"query must be rewritten first"
argument_list|)
throw|;
block|}
if|if
condition|(
name|values
operator|==
literal|null
operator|||
name|values
operator|.
name|isEmpty
argument_list|()
condition|)
block|{
return|return
name|Queries
operator|.
name|newMatchNoDocsQuery
argument_list|(
literal|"No terms supplied for "
operator|+
name|getName
argument_list|()
argument_list|)
return|;
block|}
return|return
name|handleTermsQuery
argument_list|(
name|values
argument_list|,
name|fieldName
argument_list|,
name|context
argument_list|)
return|;
block|}
DECL|method|fetch
specifier|private
name|List
argument_list|<
name|Object
argument_list|>
name|fetch
parameter_list|(
name|TermsLookup
name|termsLookup
parameter_list|,
name|Client
name|client
parameter_list|)
block|{
name|List
argument_list|<
name|Object
argument_list|>
name|terms
init|=
operator|new
name|ArrayList
argument_list|<>
argument_list|()
decl_stmt|;
name|GetRequest
name|getRequest
init|=
operator|new
name|GetRequest
argument_list|(
name|termsLookup
operator|.
name|index
argument_list|()
argument_list|,
name|termsLookup
operator|.
name|type
argument_list|()
argument_list|,
name|termsLookup
operator|.
name|id
argument_list|()
argument_list|)
operator|.
name|preference
argument_list|(
literal|"_local"
argument_list|)
operator|.
name|routing
argument_list|(
name|termsLookup
operator|.
name|routing
argument_list|()
argument_list|)
decl_stmt|;
specifier|final
name|GetResponse
name|getResponse
init|=
name|client
operator|.
name|get
argument_list|(
name|getRequest
argument_list|)
operator|.
name|actionGet
argument_list|()
decl_stmt|;
if|if
condition|(
name|getResponse
operator|.
name|isExists
argument_list|()
condition|)
block|{
name|List
argument_list|<
name|Object
argument_list|>
name|extractedValues
init|=
name|XContentMapValues
operator|.
name|extractRawValues
argument_list|(
name|termsLookup
operator|.
name|path
argument_list|()
argument_list|,
name|getResponse
operator|.
name|getSourceAsMap
argument_list|()
argument_list|)
decl_stmt|;
name|terms
operator|.
name|addAll
argument_list|(
name|extractedValues
argument_list|)
expr_stmt|;
block|}
return|return
name|terms
return|;
block|}
DECL|method|handleTermsQuery
specifier|private
specifier|static
name|Query
name|handleTermsQuery
parameter_list|(
name|List
argument_list|<
name|?
argument_list|>
name|terms
parameter_list|,
name|String
name|fieldName
parameter_list|,
name|QueryShardContext
name|context
parameter_list|)
block|{
name|MappedFieldType
name|fieldType
init|=
name|context
operator|.
name|fieldMapper
argument_list|(
name|fieldName
argument_list|)
decl_stmt|;
name|String
name|indexFieldName
decl_stmt|;
if|if
condition|(
name|fieldType
operator|!=
literal|null
condition|)
block|{
name|indexFieldName
operator|=
name|fieldType
operator|.
name|name
argument_list|()
expr_stmt|;
block|}
else|else
block|{
name|indexFieldName
operator|=
name|fieldName
expr_stmt|;
block|}
name|Query
name|query
decl_stmt|;
if|if
condition|(
name|context
operator|.
name|isFilter
argument_list|()
condition|)
block|{
if|if
condition|(
name|fieldType
operator|!=
literal|null
condition|)
block|{
name|query
operator|=
name|fieldType
operator|.
name|termsQuery
argument_list|(
name|terms
argument_list|,
name|context
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|BytesRef
index|[]
name|filterValues
init|=
operator|new
name|BytesRef
index|[
name|terms
operator|.
name|size
argument_list|()
index|]
decl_stmt|;
for|for
control|(
name|int
name|i
init|=
literal|0
init|;
name|i
operator|<
name|filterValues
operator|.
name|length
condition|;
name|i
operator|++
control|)
block|{
name|filterValues
index|[
name|i
index|]
operator|=
name|BytesRefs
operator|.
name|toBytesRef
argument_list|(
name|terms
operator|.
name|get
argument_list|(
name|i
argument_list|)
argument_list|)
expr_stmt|;
block|}
name|query
operator|=
operator|new
name|TermsQuery
argument_list|(
name|indexFieldName
argument_list|,
name|filterValues
argument_list|)
expr_stmt|;
block|}
block|}
else|else
block|{
name|BooleanQuery
operator|.
name|Builder
name|bq
init|=
operator|new
name|BooleanQuery
operator|.
name|Builder
argument_list|()
decl_stmt|;
for|for
control|(
name|Object
name|term
range|:
name|terms
control|)
block|{
if|if
condition|(
name|fieldType
operator|!=
literal|null
condition|)
block|{
name|bq
operator|.
name|add
argument_list|(
name|fieldType
operator|.
name|termQuery
argument_list|(
name|term
argument_list|,
name|context
argument_list|)
argument_list|,
name|BooleanClause
operator|.
name|Occur
operator|.
name|SHOULD
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|bq
operator|.
name|add
argument_list|(
operator|new
name|TermQuery
argument_list|(
operator|new
name|Term
argument_list|(
name|indexFieldName
argument_list|,
name|BytesRefs
operator|.
name|toBytesRef
argument_list|(
name|term
argument_list|)
argument_list|)
argument_list|)
argument_list|,
name|BooleanClause
operator|.
name|Occur
operator|.
name|SHOULD
argument_list|)
expr_stmt|;
block|}
block|}
name|query
operator|=
name|bq
operator|.
name|build
argument_list|()
expr_stmt|;
block|}
return|return
name|query
return|;
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
name|fieldName
argument_list|,
name|values
argument_list|,
name|termsLookup
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
name|TermsQueryBuilder
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
name|values
argument_list|,
name|other
operator|.
name|values
argument_list|)
operator|&&
name|Objects
operator|.
name|equals
argument_list|(
name|termsLookup
argument_list|,
name|other
operator|.
name|termsLookup
argument_list|)
return|;
block|}
annotation|@
name|Override
DECL|method|doRewrite
specifier|protected
name|QueryBuilder
argument_list|<
name|?
argument_list|>
name|doRewrite
parameter_list|(
name|QueryRewriteContext
name|queryRewriteContext
parameter_list|)
throws|throws
name|IOException
block|{
if|if
condition|(
name|this
operator|.
name|termsLookup
operator|!=
literal|null
condition|)
block|{
name|TermsLookup
name|termsLookup
init|=
operator|new
name|TermsLookup
argument_list|(
name|this
operator|.
name|termsLookup
argument_list|)
decl_stmt|;
if|if
condition|(
name|termsLookup
operator|.
name|index
argument_list|()
operator|==
literal|null
condition|)
block|{
comment|// TODO this should go away?
if|if
condition|(
name|queryRewriteContext
operator|.
name|getIndexSettings
argument_list|()
operator|!=
literal|null
condition|)
block|{
name|termsLookup
operator|.
name|index
argument_list|(
name|queryRewriteContext
operator|.
name|getIndexSettings
argument_list|()
operator|.
name|getIndex
argument_list|()
operator|.
name|getName
argument_list|()
argument_list|)
expr_stmt|;
block|}
else|else
block|{
return|return
name|this
return|;
comment|// can't rewrite until we have index scope on the shard
block|}
block|}
name|List
argument_list|<
name|Object
argument_list|>
name|values
init|=
name|fetch
argument_list|(
name|termsLookup
argument_list|,
name|queryRewriteContext
operator|.
name|getClient
argument_list|()
argument_list|)
decl_stmt|;
return|return
operator|new
name|TermsQueryBuilder
argument_list|(
name|this
operator|.
name|fieldName
argument_list|,
name|values
argument_list|)
return|;
block|}
return|return
name|this
return|;
block|}
block|}
end_class

end_unit

