begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to Elasticsearch under one or more contributor  * license agreements. See the NOTICE file distributed with  * this work for additional information regarding copyright  * ownership. Elasticsearch licenses this file to you under  * the Apache License, Version 2.0 (the "License"); you may  * not use this file except in compliance with the License.  * You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.search.sort
package|package
name|org
operator|.
name|elasticsearch
operator|.
name|search
operator|.
name|sort
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
name|XContentParser
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
name|query
operator|.
name|QueryBuilder
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
name|query
operator|.
name|QueryParseContext
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
comment|/**  * A sort builder to sort based on a document field.  */
end_comment

begin_class
DECL|class|FieldSortBuilder
specifier|public
class|class
name|FieldSortBuilder
extends|extends
name|SortBuilder
argument_list|<
name|FieldSortBuilder
argument_list|>
implements|implements
name|SortBuilderParser
argument_list|<
name|FieldSortBuilder
argument_list|>
block|{
DECL|field|PROTOTYPE
specifier|static
specifier|final
name|FieldSortBuilder
name|PROTOTYPE
init|=
operator|new
name|FieldSortBuilder
argument_list|(
literal|""
argument_list|)
decl_stmt|;
DECL|field|NAME
specifier|public
specifier|static
specifier|final
name|String
name|NAME
init|=
literal|"field_sort"
decl_stmt|;
DECL|field|NESTED_PATH
specifier|public
specifier|static
specifier|final
name|ParseField
name|NESTED_PATH
init|=
operator|new
name|ParseField
argument_list|(
literal|"nested_path"
argument_list|)
decl_stmt|;
DECL|field|NESTED_FILTER
specifier|public
specifier|static
specifier|final
name|ParseField
name|NESTED_FILTER
init|=
operator|new
name|ParseField
argument_list|(
literal|"nested_filter"
argument_list|)
decl_stmt|;
DECL|field|MISSING
specifier|public
specifier|static
specifier|final
name|ParseField
name|MISSING
init|=
operator|new
name|ParseField
argument_list|(
literal|"missing"
argument_list|)
decl_stmt|;
DECL|field|ORDER
specifier|public
specifier|static
specifier|final
name|ParseField
name|ORDER
init|=
operator|new
name|ParseField
argument_list|(
literal|"order"
argument_list|)
decl_stmt|;
DECL|field|REVERSE
specifier|public
specifier|static
specifier|final
name|ParseField
name|REVERSE
init|=
operator|new
name|ParseField
argument_list|(
literal|"reverse"
argument_list|)
decl_stmt|;
DECL|field|SORT_MODE
specifier|public
specifier|static
specifier|final
name|ParseField
name|SORT_MODE
init|=
operator|new
name|ParseField
argument_list|(
literal|"mode"
argument_list|)
decl_stmt|;
DECL|field|UNMAPPED_TYPE
specifier|public
specifier|static
specifier|final
name|ParseField
name|UNMAPPED_TYPE
init|=
operator|new
name|ParseField
argument_list|(
literal|"unmapped_type"
argument_list|)
decl_stmt|;
DECL|field|fieldName
specifier|private
specifier|final
name|String
name|fieldName
decl_stmt|;
DECL|field|missing
specifier|private
name|Object
name|missing
decl_stmt|;
DECL|field|unmappedType
specifier|private
name|String
name|unmappedType
decl_stmt|;
DECL|field|sortMode
specifier|private
name|String
name|sortMode
decl_stmt|;
DECL|field|nestedFilter
specifier|private
name|QueryBuilder
name|nestedFilter
decl_stmt|;
DECL|field|nestedPath
specifier|private
name|String
name|nestedPath
decl_stmt|;
comment|/** Copy constructor. */
DECL|method|FieldSortBuilder
specifier|public
name|FieldSortBuilder
parameter_list|(
name|FieldSortBuilder
name|template
parameter_list|)
block|{
name|this
argument_list|(
name|template
operator|.
name|fieldName
argument_list|)
expr_stmt|;
name|this
operator|.
name|order
argument_list|(
name|template
operator|.
name|order
argument_list|()
argument_list|)
expr_stmt|;
name|this
operator|.
name|missing
argument_list|(
name|template
operator|.
name|missing
argument_list|()
argument_list|)
expr_stmt|;
name|this
operator|.
name|unmappedType
argument_list|(
name|template
operator|.
name|unmappedType
argument_list|()
argument_list|)
expr_stmt|;
name|this
operator|.
name|sortMode
argument_list|(
name|template
operator|.
name|sortMode
argument_list|()
argument_list|)
expr_stmt|;
name|this
operator|.
name|setNestedFilter
argument_list|(
name|template
operator|.
name|getNestedFilter
argument_list|()
argument_list|)
expr_stmt|;
name|this
operator|.
name|setNestedPath
argument_list|(
name|template
operator|.
name|getNestedPath
argument_list|()
argument_list|)
expr_stmt|;
block|}
comment|/**      * Constructs a new sort based on a document field.      *      * @param fieldName      *            The field name.      */
DECL|method|FieldSortBuilder
specifier|public
name|FieldSortBuilder
parameter_list|(
name|String
name|fieldName
parameter_list|)
block|{
if|if
condition|(
name|fieldName
operator|==
literal|null
condition|)
block|{
throw|throw
operator|new
name|IllegalArgumentException
argument_list|(
literal|"fieldName must not be null"
argument_list|)
throw|;
block|}
name|this
operator|.
name|fieldName
operator|=
name|fieldName
expr_stmt|;
block|}
comment|/** Returns the document field this sort should be based on. */
DECL|method|getFieldName
specifier|public
name|String
name|getFieldName
parameter_list|()
block|{
return|return
name|this
operator|.
name|fieldName
return|;
block|}
comment|/**      * Sets the value when a field is missing in a doc. Can also be set to<tt>_last</tt> or      *<tt>_first</tt> to sort missing last or first respectively.      */
DECL|method|missing
specifier|public
name|FieldSortBuilder
name|missing
parameter_list|(
name|Object
name|missing
parameter_list|)
block|{
if|if
condition|(
name|missing
operator|instanceof
name|String
condition|)
block|{
name|this
operator|.
name|missing
operator|=
name|BytesRefs
operator|.
name|toBytesRef
argument_list|(
name|missing
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|this
operator|.
name|missing
operator|=
name|missing
expr_stmt|;
block|}
return|return
name|this
return|;
block|}
comment|/** Returns the value used when a field is missing in a doc. */
DECL|method|missing
specifier|public
name|Object
name|missing
parameter_list|()
block|{
if|if
condition|(
name|missing
operator|instanceof
name|BytesRef
condition|)
block|{
return|return
operator|(
operator|(
name|BytesRef
operator|)
name|missing
operator|)
operator|.
name|utf8ToString
argument_list|()
return|;
block|}
return|return
name|missing
return|;
block|}
comment|/**      * Set the type to use in case the current field is not mapped in an index.      * Specifying a type tells Elasticsearch what type the sort values should      * have, which is important for cross-index search, if there are sort fields      * that exist on some indices only. If the unmapped type is<tt>null</tt>      * then query execution will fail if one or more indices don't have a      * mapping for the current field.      */
DECL|method|unmappedType
specifier|public
name|FieldSortBuilder
name|unmappedType
parameter_list|(
name|String
name|type
parameter_list|)
block|{
name|this
operator|.
name|unmappedType
operator|=
name|type
expr_stmt|;
return|return
name|this
return|;
block|}
comment|/**      * Returns the type to use in case the current field is not mapped in an      * index.      */
DECL|method|unmappedType
specifier|public
name|String
name|unmappedType
parameter_list|()
block|{
return|return
name|this
operator|.
name|unmappedType
return|;
block|}
comment|/**      * Defines what values to pick in the case a document contains multiple      * values for the targeted sort field. Possible values: min, max, sum and      * avg      *       * TODO would love to see an enum here      *<p>      * The last two values are only applicable for number based fields.      */
DECL|method|sortMode
specifier|public
name|FieldSortBuilder
name|sortMode
parameter_list|(
name|String
name|sortMode
parameter_list|)
block|{
name|this
operator|.
name|sortMode
operator|=
name|sortMode
expr_stmt|;
return|return
name|this
return|;
block|}
comment|/**      * Returns what values to pick in the case a document contains multiple      * values for the targeted sort field.      */
DECL|method|sortMode
specifier|public
name|String
name|sortMode
parameter_list|()
block|{
return|return
name|this
operator|.
name|sortMode
return|;
block|}
comment|/**      * Sets the nested filter that the nested objects should match with in order      * to be taken into account for sorting.      *       * TODO should the above getters and setters be deprecated/ changed in      * favour of real getters and setters?      */
DECL|method|setNestedFilter
specifier|public
name|FieldSortBuilder
name|setNestedFilter
parameter_list|(
name|QueryBuilder
name|nestedFilter
parameter_list|)
block|{
name|this
operator|.
name|nestedFilter
operator|=
name|nestedFilter
expr_stmt|;
return|return
name|this
return|;
block|}
comment|/**      * Returns the nested filter that the nested objects should match with in      * order to be taken into account for sorting.      */
DECL|method|getNestedFilter
specifier|public
name|QueryBuilder
name|getNestedFilter
parameter_list|()
block|{
return|return
name|this
operator|.
name|nestedFilter
return|;
block|}
comment|/**      * Sets the nested path if sorting occurs on a field that is inside a nested      * object. By default when sorting on a field inside a nested object, the      * nearest upper nested object is selected as nested path.      */
DECL|method|setNestedPath
specifier|public
name|FieldSortBuilder
name|setNestedPath
parameter_list|(
name|String
name|nestedPath
parameter_list|)
block|{
name|this
operator|.
name|nestedPath
operator|=
name|nestedPath
expr_stmt|;
return|return
name|this
return|;
block|}
comment|/**      * Returns the nested path if sorting occurs in a field that is inside a      * nested object.      */
DECL|method|getNestedPath
specifier|public
name|String
name|getNestedPath
parameter_list|()
block|{
return|return
name|this
operator|.
name|nestedPath
return|;
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
argument_list|(
name|fieldName
argument_list|)
expr_stmt|;
name|builder
operator|.
name|field
argument_list|(
name|ORDER_FIELD
operator|.
name|getPreferredName
argument_list|()
argument_list|,
name|order
argument_list|)
expr_stmt|;
if|if
condition|(
name|missing
operator|!=
literal|null
condition|)
block|{
if|if
condition|(
name|missing
operator|instanceof
name|BytesRef
condition|)
block|{
name|builder
operator|.
name|field
argument_list|(
name|MISSING
operator|.
name|getPreferredName
argument_list|()
argument_list|,
operator|(
operator|(
name|BytesRef
operator|)
name|missing
operator|)
operator|.
name|utf8ToString
argument_list|()
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|builder
operator|.
name|field
argument_list|(
name|MISSING
operator|.
name|getPreferredName
argument_list|()
argument_list|,
name|missing
argument_list|)
expr_stmt|;
block|}
block|}
if|if
condition|(
name|unmappedType
operator|!=
literal|null
condition|)
block|{
name|builder
operator|.
name|field
argument_list|(
name|UNMAPPED_TYPE
operator|.
name|getPreferredName
argument_list|()
argument_list|,
name|unmappedType
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|sortMode
operator|!=
literal|null
condition|)
block|{
name|builder
operator|.
name|field
argument_list|(
name|SORT_MODE
operator|.
name|getPreferredName
argument_list|()
argument_list|,
name|sortMode
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|nestedFilter
operator|!=
literal|null
condition|)
block|{
name|builder
operator|.
name|field
argument_list|(
name|NESTED_FILTER
operator|.
name|getPreferredName
argument_list|()
argument_list|,
name|nestedFilter
argument_list|,
name|params
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|nestedPath
operator|!=
literal|null
condition|)
block|{
name|builder
operator|.
name|field
argument_list|(
name|NESTED_PATH
operator|.
name|getPreferredName
argument_list|()
argument_list|,
name|nestedPath
argument_list|)
expr_stmt|;
block|}
name|builder
operator|.
name|endObject
argument_list|()
expr_stmt|;
return|return
name|builder
return|;
block|}
annotation|@
name|Override
DECL|method|equals
specifier|public
name|boolean
name|equals
parameter_list|(
name|Object
name|other
parameter_list|)
block|{
if|if
condition|(
name|this
operator|==
name|other
condition|)
block|{
return|return
literal|true
return|;
block|}
if|if
condition|(
name|other
operator|==
literal|null
operator|||
name|getClass
argument_list|()
operator|!=
name|other
operator|.
name|getClass
argument_list|()
condition|)
block|{
return|return
literal|false
return|;
block|}
name|FieldSortBuilder
name|builder
init|=
operator|(
name|FieldSortBuilder
operator|)
name|other
decl_stmt|;
return|return
operator|(
name|Objects
operator|.
name|equals
argument_list|(
name|this
operator|.
name|fieldName
argument_list|,
name|builder
operator|.
name|fieldName
argument_list|)
operator|&&
name|Objects
operator|.
name|equals
argument_list|(
name|this
operator|.
name|nestedFilter
argument_list|,
name|builder
operator|.
name|nestedFilter
argument_list|)
operator|&&
name|Objects
operator|.
name|equals
argument_list|(
name|this
operator|.
name|nestedPath
argument_list|,
name|builder
operator|.
name|nestedPath
argument_list|)
operator|&&
name|Objects
operator|.
name|equals
argument_list|(
name|this
operator|.
name|missing
argument_list|,
name|builder
operator|.
name|missing
argument_list|)
operator|&&
name|Objects
operator|.
name|equals
argument_list|(
name|this
operator|.
name|order
argument_list|,
name|builder
operator|.
name|order
argument_list|)
operator|&&
name|Objects
operator|.
name|equals
argument_list|(
name|this
operator|.
name|sortMode
argument_list|,
name|builder
operator|.
name|sortMode
argument_list|)
operator|&&
name|Objects
operator|.
name|equals
argument_list|(
name|this
operator|.
name|unmappedType
argument_list|,
name|builder
operator|.
name|unmappedType
argument_list|)
operator|)
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
name|this
operator|.
name|fieldName
argument_list|,
name|this
operator|.
name|nestedFilter
argument_list|,
name|this
operator|.
name|nestedPath
argument_list|,
name|this
operator|.
name|missing
argument_list|,
name|this
operator|.
name|order
argument_list|,
name|this
operator|.
name|sortMode
argument_list|,
name|this
operator|.
name|unmappedType
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
name|this
operator|.
name|fieldName
argument_list|)
expr_stmt|;
if|if
condition|(
name|this
operator|.
name|nestedFilter
operator|!=
literal|null
condition|)
block|{
name|out
operator|.
name|writeBoolean
argument_list|(
literal|true
argument_list|)
expr_stmt|;
name|out
operator|.
name|writeQuery
argument_list|(
name|this
operator|.
name|nestedFilter
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|out
operator|.
name|writeBoolean
argument_list|(
literal|false
argument_list|)
expr_stmt|;
block|}
name|out
operator|.
name|writeOptionalString
argument_list|(
name|this
operator|.
name|nestedPath
argument_list|)
expr_stmt|;
name|out
operator|.
name|writeGenericValue
argument_list|(
name|this
operator|.
name|missing
argument_list|)
expr_stmt|;
if|if
condition|(
name|this
operator|.
name|order
operator|!=
literal|null
condition|)
block|{
name|out
operator|.
name|writeBoolean
argument_list|(
literal|true
argument_list|)
expr_stmt|;
name|this
operator|.
name|order
operator|.
name|writeTo
argument_list|(
name|out
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|out
operator|.
name|writeBoolean
argument_list|(
literal|false
argument_list|)
expr_stmt|;
block|}
name|out
operator|.
name|writeOptionalString
argument_list|(
name|this
operator|.
name|sortMode
argument_list|)
expr_stmt|;
name|out
operator|.
name|writeOptionalString
argument_list|(
name|this
operator|.
name|unmappedType
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
DECL|method|readFrom
specifier|public
name|FieldSortBuilder
name|readFrom
parameter_list|(
name|StreamInput
name|in
parameter_list|)
throws|throws
name|IOException
block|{
name|String
name|fieldName
init|=
name|in
operator|.
name|readString
argument_list|()
decl_stmt|;
name|FieldSortBuilder
name|result
init|=
operator|new
name|FieldSortBuilder
argument_list|(
name|fieldName
argument_list|)
decl_stmt|;
if|if
condition|(
name|in
operator|.
name|readBoolean
argument_list|()
condition|)
block|{
name|QueryBuilder
name|query
init|=
name|in
operator|.
name|readQuery
argument_list|()
decl_stmt|;
name|result
operator|.
name|setNestedFilter
argument_list|(
name|query
argument_list|)
expr_stmt|;
block|}
name|result
operator|.
name|setNestedPath
argument_list|(
name|in
operator|.
name|readOptionalString
argument_list|()
argument_list|)
expr_stmt|;
name|result
operator|.
name|missing
argument_list|(
name|in
operator|.
name|readGenericValue
argument_list|()
argument_list|)
expr_stmt|;
if|if
condition|(
name|in
operator|.
name|readBoolean
argument_list|()
condition|)
block|{
name|result
operator|.
name|order
argument_list|(
name|SortOrder
operator|.
name|readOrderFrom
argument_list|(
name|in
argument_list|)
argument_list|)
expr_stmt|;
block|}
name|result
operator|.
name|sortMode
argument_list|(
name|in
operator|.
name|readOptionalString
argument_list|()
argument_list|)
expr_stmt|;
name|result
operator|.
name|unmappedType
argument_list|(
name|in
operator|.
name|readOptionalString
argument_list|()
argument_list|)
expr_stmt|;
return|return
name|result
return|;
block|}
annotation|@
name|Override
DECL|method|fromXContent
specifier|public
name|FieldSortBuilder
name|fromXContent
parameter_list|(
name|QueryParseContext
name|context
parameter_list|,
name|String
name|fieldName
parameter_list|)
throws|throws
name|IOException
block|{
name|XContentParser
name|parser
init|=
name|context
operator|.
name|parser
argument_list|()
decl_stmt|;
name|QueryBuilder
name|nestedFilter
init|=
literal|null
decl_stmt|;
name|String
name|nestedPath
init|=
literal|null
decl_stmt|;
name|Object
name|missing
init|=
literal|null
decl_stmt|;
name|SortOrder
name|order
init|=
literal|null
decl_stmt|;
name|String
name|sortMode
init|=
literal|null
decl_stmt|;
name|String
name|unmappedType
init|=
literal|null
decl_stmt|;
name|String
name|currentFieldName
init|=
literal|null
decl_stmt|;
name|XContentParser
operator|.
name|Token
name|token
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
name|context
operator|.
name|parseFieldMatcher
argument_list|()
operator|.
name|match
argument_list|(
name|currentFieldName
argument_list|,
name|NESTED_FILTER
argument_list|)
condition|)
block|{
name|nestedFilter
operator|=
name|context
operator|.
name|parseInnerQueryBuilder
argument_list|()
expr_stmt|;
block|}
else|else
block|{
throw|throw
operator|new
name|IllegalArgumentException
argument_list|(
literal|"Expected "
operator|+
name|NESTED_FILTER
operator|.
name|getPreferredName
argument_list|()
operator|+
literal|" element."
argument_list|)
throw|;
block|}
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
name|context
operator|.
name|parseFieldMatcher
argument_list|()
operator|.
name|match
argument_list|(
name|currentFieldName
argument_list|,
name|NESTED_PATH
argument_list|)
condition|)
block|{
name|nestedPath
operator|=
name|parser
operator|.
name|text
argument_list|()
expr_stmt|;
block|}
elseif|else
if|if
condition|(
name|context
operator|.
name|parseFieldMatcher
argument_list|()
operator|.
name|match
argument_list|(
name|currentFieldName
argument_list|,
name|MISSING
argument_list|)
condition|)
block|{
name|missing
operator|=
name|parser
operator|.
name|objectBytes
argument_list|()
expr_stmt|;
block|}
elseif|else
if|if
condition|(
name|context
operator|.
name|parseFieldMatcher
argument_list|()
operator|.
name|match
argument_list|(
name|currentFieldName
argument_list|,
name|REVERSE
argument_list|)
condition|)
block|{
if|if
condition|(
name|parser
operator|.
name|booleanValue
argument_list|()
condition|)
block|{
name|order
operator|=
name|SortOrder
operator|.
name|DESC
expr_stmt|;
block|}
comment|// else we keep the default ASC
block|}
elseif|else
if|if
condition|(
name|context
operator|.
name|parseFieldMatcher
argument_list|()
operator|.
name|match
argument_list|(
name|currentFieldName
argument_list|,
name|ORDER
argument_list|)
condition|)
block|{
name|String
name|sortOrder
init|=
name|parser
operator|.
name|text
argument_list|()
decl_stmt|;
if|if
condition|(
literal|"asc"
operator|.
name|equals
argument_list|(
name|sortOrder
argument_list|)
condition|)
block|{
name|order
operator|=
name|SortOrder
operator|.
name|ASC
expr_stmt|;
block|}
elseif|else
if|if
condition|(
literal|"desc"
operator|.
name|equals
argument_list|(
name|sortOrder
argument_list|)
condition|)
block|{
name|order
operator|=
name|SortOrder
operator|.
name|DESC
expr_stmt|;
block|}
else|else
block|{
throw|throw
operator|new
name|IllegalStateException
argument_list|(
literal|"Sort order "
operator|+
name|sortOrder
operator|+
literal|" not supported."
argument_list|)
throw|;
block|}
block|}
elseif|else
if|if
condition|(
name|context
operator|.
name|parseFieldMatcher
argument_list|()
operator|.
name|match
argument_list|(
name|currentFieldName
argument_list|,
name|SORT_MODE
argument_list|)
condition|)
block|{
name|sortMode
operator|=
name|parser
operator|.
name|text
argument_list|()
expr_stmt|;
block|}
elseif|else
if|if
condition|(
name|context
operator|.
name|parseFieldMatcher
argument_list|()
operator|.
name|match
argument_list|(
name|currentFieldName
argument_list|,
name|UNMAPPED_TYPE
argument_list|)
condition|)
block|{
name|unmappedType
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
name|IllegalArgumentException
argument_list|(
literal|"Option "
operator|+
name|currentFieldName
operator|+
literal|" not supported."
argument_list|)
throw|;
block|}
block|}
block|}
name|FieldSortBuilder
name|builder
init|=
operator|new
name|FieldSortBuilder
argument_list|(
name|fieldName
argument_list|)
decl_stmt|;
if|if
condition|(
name|nestedFilter
operator|!=
literal|null
condition|)
block|{
name|builder
operator|.
name|setNestedFilter
argument_list|(
name|nestedFilter
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|nestedPath
operator|!=
literal|null
condition|)
block|{
name|builder
operator|.
name|setNestedPath
argument_list|(
name|nestedPath
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|missing
operator|!=
literal|null
condition|)
block|{
name|builder
operator|.
name|missing
argument_list|(
name|missing
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|order
operator|!=
literal|null
condition|)
block|{
name|builder
operator|.
name|order
argument_list|(
name|order
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|sortMode
operator|!=
literal|null
condition|)
block|{
name|builder
operator|.
name|sortMode
argument_list|(
name|sortMode
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|unmappedType
operator|!=
literal|null
condition|)
block|{
name|builder
operator|.
name|unmappedType
argument_list|(
name|unmappedType
argument_list|)
expr_stmt|;
block|}
return|return
name|builder
return|;
block|}
block|}
end_class

end_unit

