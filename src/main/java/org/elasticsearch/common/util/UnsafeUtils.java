begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to Elasticsearch under one or more contributor  * license agreements. See the NOTICE file distributed with  * this work for additional information regarding copyright  * ownership. Elasticsearch licenses this file to you under  * the Apache License, Version 2.0 (the "License"); you may  * not use this file except in compliance with the License.  * You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.common.util
package|package
name|org
operator|.
name|elasticsearch
operator|.
name|common
operator|.
name|util
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
name|sun
operator|.
name|misc
operator|.
name|Unsafe
import|;
end_import

begin_import
import|import
name|java
operator|.
name|lang
operator|.
name|reflect
operator|.
name|Field
import|;
end_import

begin_import
import|import
name|java
operator|.
name|nio
operator|.
name|ByteOrder
import|;
end_import

begin_comment
comment|/** Utility methods that use {@link Unsafe}. */
end_comment

begin_enum
DECL|enum|UnsafeUtils
specifier|public
enum|enum
name|UnsafeUtils
block|{     ;
DECL|field|UNSAFE
specifier|private
specifier|static
specifier|final
name|Unsafe
name|UNSAFE
decl_stmt|;
DECL|field|BYTE_ARRAY_OFFSET
specifier|private
specifier|static
specifier|final
name|long
name|BYTE_ARRAY_OFFSET
decl_stmt|;
DECL|field|BYTE_ARRAY_SCALE
specifier|private
specifier|static
specifier|final
name|int
name|BYTE_ARRAY_SCALE
decl_stmt|;
static|static
block|{
try|try
block|{
name|Field
name|theUnsafe
init|=
name|Unsafe
operator|.
name|class
operator|.
name|getDeclaredField
argument_list|(
literal|"theUnsafe"
argument_list|)
decl_stmt|;
name|theUnsafe
operator|.
name|setAccessible
argument_list|(
literal|true
argument_list|)
expr_stmt|;
name|UNSAFE
operator|=
operator|(
name|Unsafe
operator|)
name|theUnsafe
operator|.
name|get
argument_list|(
literal|null
argument_list|)
expr_stmt|;
name|BYTE_ARRAY_OFFSET
operator|=
name|UNSAFE
operator|.
name|arrayBaseOffset
argument_list|(
name|byte
index|[]
operator|.
expr|class
argument_list|)
expr_stmt|;
name|BYTE_ARRAY_SCALE
operator|=
name|UNSAFE
operator|.
name|arrayIndexScale
argument_list|(
name|byte
index|[]
operator|.
expr|class
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|IllegalAccessException
name|e
parameter_list|)
block|{
throw|throw
operator|new
name|ExceptionInInitializerError
argument_list|(
literal|"Cannot access Unsafe"
argument_list|)
throw|;
block|}
catch|catch
parameter_list|(
name|NoSuchFieldException
name|e
parameter_list|)
block|{
throw|throw
operator|new
name|ExceptionInInitializerError
argument_list|(
literal|"Cannot access Unsafe"
argument_list|)
throw|;
block|}
catch|catch
parameter_list|(
name|SecurityException
name|e
parameter_list|)
block|{
throw|throw
operator|new
name|ExceptionInInitializerError
argument_list|(
literal|"Cannot access Unsafe"
argument_list|)
throw|;
block|}
block|}
comment|// Don't expose these methods directly, they are too easy to mis-use since they depend on the byte order.
comment|// If you need methods to read integers, please expose a method that makes the byte order explicit such
comment|// as readIntLE (little endian).
comment|// Also, please ***NEVER*** expose any method that writes using Unsafe, this is too dangerous
DECL|method|readLong
specifier|private
specifier|static
name|long
name|readLong
parameter_list|(
name|byte
index|[]
name|src
parameter_list|,
name|int
name|offset
parameter_list|)
block|{
return|return
name|UNSAFE
operator|.
name|getLong
argument_list|(
name|src
argument_list|,
name|BYTE_ARRAY_OFFSET
operator|+
name|offset
argument_list|)
return|;
block|}
DECL|method|readInt
specifier|private
specifier|static
name|int
name|readInt
parameter_list|(
name|byte
index|[]
name|src
parameter_list|,
name|int
name|offset
parameter_list|)
block|{
return|return
name|UNSAFE
operator|.
name|getInt
argument_list|(
name|src
argument_list|,
name|BYTE_ARRAY_OFFSET
operator|+
name|offset
argument_list|)
return|;
block|}
DECL|method|readShort
specifier|private
specifier|static
name|short
name|readShort
parameter_list|(
name|byte
index|[]
name|src
parameter_list|,
name|int
name|offset
parameter_list|)
block|{
return|return
name|UNSAFE
operator|.
name|getShort
argument_list|(
name|src
argument_list|,
name|BYTE_ARRAY_OFFSET
operator|+
name|offset
argument_list|)
return|;
block|}
DECL|method|readByte
specifier|private
specifier|static
name|byte
name|readByte
parameter_list|(
name|byte
index|[]
name|src
parameter_list|,
name|int
name|offset
parameter_list|)
block|{
return|return
name|UNSAFE
operator|.
name|getByte
argument_list|(
name|src
argument_list|,
name|BYTE_ARRAY_OFFSET
operator|+
name|BYTE_ARRAY_SCALE
operator|*
name|offset
argument_list|)
return|;
block|}
comment|/** Compare the two given {@link BytesRef}s for equality. */
DECL|method|equals
specifier|public
specifier|static
name|boolean
name|equals
parameter_list|(
name|BytesRef
name|b1
parameter_list|,
name|BytesRef
name|b2
parameter_list|)
block|{
if|if
condition|(
name|b1
operator|.
name|length
operator|!=
name|b2
operator|.
name|length
condition|)
block|{
return|return
literal|false
return|;
block|}
return|return
name|equals
argument_list|(
name|b1
operator|.
name|bytes
argument_list|,
name|b1
operator|.
name|offset
argument_list|,
name|b2
operator|.
name|bytes
argument_list|,
name|b2
operator|.
name|offset
argument_list|,
name|b1
operator|.
name|length
argument_list|)
return|;
block|}
comment|/**      * Compare<code>b1[offset1:offset1+length)</code>against<code>b1[offset2:offset2+length)</code>.      */
DECL|method|equals
specifier|public
specifier|static
name|boolean
name|equals
parameter_list|(
name|byte
index|[]
name|b1
parameter_list|,
name|int
name|offset1
parameter_list|,
name|byte
index|[]
name|b2
parameter_list|,
name|int
name|offset2
parameter_list|,
name|int
name|length
parameter_list|)
block|{
name|int
name|o1
init|=
name|offset1
decl_stmt|;
name|int
name|o2
init|=
name|offset2
decl_stmt|;
name|int
name|len
init|=
name|length
decl_stmt|;
while|while
condition|(
name|len
operator|>=
literal|8
condition|)
block|{
if|if
condition|(
name|readLong
argument_list|(
name|b1
argument_list|,
name|o1
argument_list|)
operator|!=
name|readLong
argument_list|(
name|b2
argument_list|,
name|o2
argument_list|)
condition|)
block|{
return|return
literal|false
return|;
block|}
name|len
operator|-=
literal|8
expr_stmt|;
name|o1
operator|+=
literal|8
expr_stmt|;
name|o2
operator|+=
literal|8
expr_stmt|;
block|}
if|if
condition|(
name|len
operator|>=
literal|4
condition|)
block|{
if|if
condition|(
name|readInt
argument_list|(
name|b1
argument_list|,
name|o1
argument_list|)
operator|!=
name|readInt
argument_list|(
name|b2
argument_list|,
name|o2
argument_list|)
condition|)
block|{
return|return
literal|false
return|;
block|}
name|len
operator|-=
literal|4
expr_stmt|;
name|o1
operator|+=
literal|4
expr_stmt|;
name|o2
operator|+=
literal|4
expr_stmt|;
block|}
if|if
condition|(
name|len
operator|>=
literal|2
condition|)
block|{
if|if
condition|(
name|readShort
argument_list|(
name|b1
argument_list|,
name|o1
argument_list|)
operator|!=
name|readShort
argument_list|(
name|b2
argument_list|,
name|o2
argument_list|)
condition|)
block|{
return|return
literal|false
return|;
block|}
name|len
operator|-=
literal|2
expr_stmt|;
name|o1
operator|+=
literal|2
expr_stmt|;
name|o2
operator|+=
literal|2
expr_stmt|;
block|}
if|if
condition|(
name|len
operator|==
literal|1
condition|)
block|{
if|if
condition|(
name|readByte
argument_list|(
name|b1
argument_list|,
name|o1
argument_list|)
operator|!=
name|readByte
argument_list|(
name|b2
argument_list|,
name|o2
argument_list|)
condition|)
block|{
return|return
literal|false
return|;
block|}
block|}
else|else
block|{
assert|assert
name|len
operator|==
literal|0
assert|;
block|}
return|return
literal|true
return|;
block|}
comment|/**      * Read a long using little endian byte order.      */
DECL|method|readLongLE
specifier|public
specifier|static
name|long
name|readLongLE
parameter_list|(
name|byte
index|[]
name|src
parameter_list|,
name|int
name|offset
parameter_list|)
block|{
name|long
name|value
init|=
name|readLong
argument_list|(
name|src
argument_list|,
name|offset
argument_list|)
decl_stmt|;
if|if
condition|(
name|ByteOrder
operator|.
name|nativeOrder
argument_list|()
operator|==
name|ByteOrder
operator|.
name|BIG_ENDIAN
condition|)
block|{
name|value
operator|=
name|Long
operator|.
name|reverseBytes
argument_list|(
name|value
argument_list|)
expr_stmt|;
block|}
return|return
name|value
return|;
block|}
comment|/**      * Read an int using little endian byte order.      */
DECL|method|readIntLE
specifier|public
specifier|static
name|int
name|readIntLE
parameter_list|(
name|byte
index|[]
name|src
parameter_list|,
name|int
name|offset
parameter_list|)
block|{
name|int
name|value
init|=
name|readInt
argument_list|(
name|src
argument_list|,
name|offset
argument_list|)
decl_stmt|;
if|if
condition|(
name|ByteOrder
operator|.
name|nativeOrder
argument_list|()
operator|==
name|ByteOrder
operator|.
name|BIG_ENDIAN
condition|)
block|{
name|value
operator|=
name|Integer
operator|.
name|reverseBytes
argument_list|(
name|value
argument_list|)
expr_stmt|;
block|}
return|return
name|value
return|;
block|}
block|}
end_enum

end_unit

