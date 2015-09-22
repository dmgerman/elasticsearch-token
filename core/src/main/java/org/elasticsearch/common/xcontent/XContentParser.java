begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to Elasticsearch under one or more contributor  * license agreements. See the NOTICE file distributed with  * this work for additional information regarding copyright  * ownership. Elasticsearch licenses this file to you under  * the Apache License, Version 2.0 (the "License"); you may  * not use this file except in compliance with the License.  * You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.common.xcontent
package|package
name|org
operator|.
name|elasticsearch
operator|.
name|common
operator|.
name|xcontent
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
name|lease
operator|.
name|Releasable
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
name|List
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|Map
import|;
end_import

begin_comment
comment|/**  * Interface for pull - parsing {@link XContent} see {@link XContentType} for supported types.  *  * To obtain an instance of this class use the following pattern:  *  *<pre>  *     XContentType xContentType = XContentType.JSON;  *     XContentParser parser = xContentType.xContent().createParser("{\"key\" : \"value\"}");  *</pre>  */
end_comment

begin_interface
DECL|interface|XContentParser
specifier|public
interface|interface
name|XContentParser
extends|extends
name|Releasable
block|{
DECL|enum|Token
enum|enum
name|Token
block|{
DECL|enum constant|START_OBJECT
name|START_OBJECT
block|{
annotation|@
name|Override
specifier|public
name|boolean
name|isValue
parameter_list|()
block|{
return|return
literal|false
return|;
block|}
block|}
block|,
DECL|enum constant|END_OBJECT
name|END_OBJECT
block|{
annotation|@
name|Override
specifier|public
name|boolean
name|isValue
parameter_list|()
block|{
return|return
literal|false
return|;
block|}
block|}
block|,
DECL|enum constant|START_ARRAY
name|START_ARRAY
block|{
annotation|@
name|Override
specifier|public
name|boolean
name|isValue
parameter_list|()
block|{
return|return
literal|false
return|;
block|}
block|}
block|,
DECL|enum constant|END_ARRAY
name|END_ARRAY
block|{
annotation|@
name|Override
specifier|public
name|boolean
name|isValue
parameter_list|()
block|{
return|return
literal|false
return|;
block|}
block|}
block|,
DECL|enum constant|FIELD_NAME
name|FIELD_NAME
block|{
annotation|@
name|Override
specifier|public
name|boolean
name|isValue
parameter_list|()
block|{
return|return
literal|false
return|;
block|}
block|}
block|,
DECL|enum constant|VALUE_STRING
name|VALUE_STRING
block|{
annotation|@
name|Override
specifier|public
name|boolean
name|isValue
parameter_list|()
block|{
return|return
literal|true
return|;
block|}
block|}
block|,
DECL|enum constant|VALUE_NUMBER
name|VALUE_NUMBER
block|{
annotation|@
name|Override
specifier|public
name|boolean
name|isValue
parameter_list|()
block|{
return|return
literal|true
return|;
block|}
block|}
block|,
DECL|enum constant|VALUE_BOOLEAN
name|VALUE_BOOLEAN
block|{
annotation|@
name|Override
specifier|public
name|boolean
name|isValue
parameter_list|()
block|{
return|return
literal|true
return|;
block|}
block|}
block|,
comment|// usually a binary value
DECL|enum constant|VALUE_EMBEDDED_OBJECT
name|VALUE_EMBEDDED_OBJECT
block|{
annotation|@
name|Override
specifier|public
name|boolean
name|isValue
parameter_list|()
block|{
return|return
literal|true
return|;
block|}
block|}
block|,
DECL|enum constant|VALUE_NULL
name|VALUE_NULL
block|{
annotation|@
name|Override
specifier|public
name|boolean
name|isValue
parameter_list|()
block|{
return|return
literal|false
return|;
block|}
block|}
block|;
DECL|method|isValue
specifier|public
specifier|abstract
name|boolean
name|isValue
parameter_list|()
function_decl|;
block|}
DECL|enum|NumberType
enum|enum
name|NumberType
block|{
DECL|enum constant|INT
DECL|enum constant|LONG
DECL|enum constant|FLOAT
DECL|enum constant|DOUBLE
name|INT
block|,
name|LONG
block|,
name|FLOAT
block|,
name|DOUBLE
block|}
DECL|method|contentType
name|XContentType
name|contentType
parameter_list|()
function_decl|;
DECL|method|nextToken
name|Token
name|nextToken
parameter_list|()
throws|throws
name|IOException
function_decl|;
DECL|method|skipChildren
name|void
name|skipChildren
parameter_list|()
throws|throws
name|IOException
function_decl|;
DECL|method|currentToken
name|Token
name|currentToken
parameter_list|()
function_decl|;
DECL|method|currentName
name|String
name|currentName
parameter_list|()
throws|throws
name|IOException
function_decl|;
DECL|method|map
name|Map
argument_list|<
name|String
argument_list|,
name|Object
argument_list|>
name|map
parameter_list|()
throws|throws
name|IOException
function_decl|;
DECL|method|mapOrdered
name|Map
argument_list|<
name|String
argument_list|,
name|Object
argument_list|>
name|mapOrdered
parameter_list|()
throws|throws
name|IOException
function_decl|;
DECL|method|list
name|List
argument_list|<
name|Object
argument_list|>
name|list
parameter_list|()
throws|throws
name|IOException
function_decl|;
DECL|method|listOrderedMap
name|List
argument_list|<
name|Object
argument_list|>
name|listOrderedMap
parameter_list|()
throws|throws
name|IOException
function_decl|;
DECL|method|text
name|String
name|text
parameter_list|()
throws|throws
name|IOException
function_decl|;
DECL|method|textOrNull
name|String
name|textOrNull
parameter_list|()
throws|throws
name|IOException
function_decl|;
comment|/**      * Returns a BytesRef holding UTF-8 bytes or null if a null value is {@link Token#VALUE_NULL}.      * This method should be used to read text only binary content should be read through {@link #binaryValue()}      */
DECL|method|utf8BytesOrNull
name|BytesRef
name|utf8BytesOrNull
parameter_list|()
throws|throws
name|IOException
function_decl|;
comment|/**      * Returns a BytesRef holding UTF-8 bytes.      * This method should be used to read text only binary content should be read through {@link #binaryValue()}      */
DECL|method|utf8Bytes
name|BytesRef
name|utf8Bytes
parameter_list|()
throws|throws
name|IOException
function_decl|;
DECL|method|objectText
name|Object
name|objectText
parameter_list|()
throws|throws
name|IOException
function_decl|;
DECL|method|objectBytes
name|Object
name|objectBytes
parameter_list|()
throws|throws
name|IOException
function_decl|;
comment|/**      * Method that can be used to determine whether calling of textCharacters() would be the most efficient way to      * access textual content for the event parser currently points to.      *      * Default implementation simply returns false since only actual      * implementation class has knowledge of its internal buffering      * state.      *      * This method shouldn't be used to check if the token contains text or not.      */
DECL|method|hasTextCharacters
name|boolean
name|hasTextCharacters
parameter_list|()
function_decl|;
DECL|method|textCharacters
name|char
index|[]
name|textCharacters
parameter_list|()
throws|throws
name|IOException
function_decl|;
DECL|method|textLength
name|int
name|textLength
parameter_list|()
throws|throws
name|IOException
function_decl|;
DECL|method|textOffset
name|int
name|textOffset
parameter_list|()
throws|throws
name|IOException
function_decl|;
DECL|method|numberValue
name|Number
name|numberValue
parameter_list|()
throws|throws
name|IOException
function_decl|;
DECL|method|numberType
name|NumberType
name|numberType
parameter_list|()
throws|throws
name|IOException
function_decl|;
comment|/**      * Is the number type estimated or not (i.e. an int might actually be a long, its just low enough      * to be an int).      */
DECL|method|estimatedNumberType
name|boolean
name|estimatedNumberType
parameter_list|()
function_decl|;
DECL|method|shortValue
name|short
name|shortValue
parameter_list|(
name|boolean
name|coerce
parameter_list|)
throws|throws
name|IOException
function_decl|;
DECL|method|intValue
name|int
name|intValue
parameter_list|(
name|boolean
name|coerce
parameter_list|)
throws|throws
name|IOException
function_decl|;
DECL|method|longValue
name|long
name|longValue
parameter_list|(
name|boolean
name|coerce
parameter_list|)
throws|throws
name|IOException
function_decl|;
DECL|method|floatValue
name|float
name|floatValue
parameter_list|(
name|boolean
name|coerce
parameter_list|)
throws|throws
name|IOException
function_decl|;
DECL|method|doubleValue
name|double
name|doubleValue
parameter_list|(
name|boolean
name|coerce
parameter_list|)
throws|throws
name|IOException
function_decl|;
DECL|method|shortValue
name|short
name|shortValue
parameter_list|()
throws|throws
name|IOException
function_decl|;
DECL|method|intValue
name|int
name|intValue
parameter_list|()
throws|throws
name|IOException
function_decl|;
DECL|method|longValue
name|long
name|longValue
parameter_list|()
throws|throws
name|IOException
function_decl|;
DECL|method|floatValue
name|float
name|floatValue
parameter_list|()
throws|throws
name|IOException
function_decl|;
DECL|method|doubleValue
name|double
name|doubleValue
parameter_list|()
throws|throws
name|IOException
function_decl|;
comment|/**      * returns true if the current value is boolean in nature.      * values that are considered booleans:      * - boolean value (true/false)      * - numeric integers (=0 is considered as false, !=0 is true)      * - one of the following strings: "true","false","on","off","yes","no","1","0"      */
DECL|method|isBooleanValue
name|boolean
name|isBooleanValue
parameter_list|()
throws|throws
name|IOException
function_decl|;
DECL|method|booleanValue
name|boolean
name|booleanValue
parameter_list|()
throws|throws
name|IOException
function_decl|;
comment|/**      * Reads a plain binary value that was written via one of the following methods:      *      *<ul>      *<li>{@link XContentBuilder#field(String, org.apache.lucene.util.BytesRef)}</li>      *<li>{@link XContentBuilder#field(String, org.elasticsearch.common.bytes.BytesReference)}</li>      *<li>{@link XContentBuilder#field(String, byte[], int, int)}}</li>      *<li>{@link XContentBuilder#field(String, byte[])}}</li>      *</ul>      *      * as well as via their<code>XContentBuilderString</code> variants of the separated value methods.      * Note: Do not use this method to read values written with:      *<ul>      *<li>{@link XContentBuilder#utf8Field(XContentBuilderString, org.apache.lucene.util.BytesRef)}</li>      *<li>{@link XContentBuilder#utf8Field(String, org.apache.lucene.util.BytesRef)}</li>      *</ul>      *      * these methods write UTF-8 encoded strings and must be read through:      *<ul>      *<li>{@link XContentParser#utf8Bytes()}</li>      *<li>{@link XContentParser#utf8BytesOrNull()}}</li>      *<li>{@link XContentParser#text()} ()}</li>      *<li>{@link XContentParser#textOrNull()} ()}</li>      *<li>{@link XContentParser#textCharacters()} ()}}</li>      *</ul>      *      */
DECL|method|binaryValue
name|byte
index|[]
name|binaryValue
parameter_list|()
throws|throws
name|IOException
function_decl|;
comment|/**      * Used for error reporting to highlight where syntax errors occur in      * content being parsed.      *       * @return last token's location or null if cannot be determined      */
DECL|method|getTokenLocation
name|XContentLocation
name|getTokenLocation
parameter_list|()
function_decl|;
DECL|method|isClosed
name|boolean
name|isClosed
parameter_list|()
function_decl|;
block|}
end_interface

end_unit

