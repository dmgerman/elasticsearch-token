begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to Elasticsearch under one or more contributor  * license agreements. See the NOTICE file distributed with  * this work for additional information regarding copyright  * ownership. Elasticsearch licenses this file to you under  * the Apache License, Version 2.0 (the "License"); you may  * not use this file except in compliance with the License.  * You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.rest.support
package|package
name|org
operator|.
name|elasticsearch
operator|.
name|rest
operator|.
name|support
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
name|Nullable
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
name|path
operator|.
name|PathTrie
import|;
end_import

begin_import
import|import
name|java
operator|.
name|nio
operator|.
name|charset
operator|.
name|Charset
import|;
end_import

begin_import
import|import
name|java
operator|.
name|nio
operator|.
name|charset
operator|.
name|StandardCharsets
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

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|regex
operator|.
name|Pattern
import|;
end_import

begin_comment
comment|/**  *  */
end_comment

begin_class
DECL|class|RestUtils
specifier|public
class|class
name|RestUtils
block|{
DECL|field|REST_DECODER
specifier|public
specifier|static
specifier|final
name|PathTrie
operator|.
name|Decoder
name|REST_DECODER
init|=
operator|new
name|PathTrie
operator|.
name|Decoder
argument_list|()
block|{
annotation|@
name|Override
specifier|public
name|String
name|decode
parameter_list|(
name|String
name|value
parameter_list|)
block|{
return|return
name|RestUtils
operator|.
name|decodeComponent
argument_list|(
name|value
argument_list|)
return|;
block|}
block|}
decl_stmt|;
DECL|method|isBrowser
specifier|public
specifier|static
name|boolean
name|isBrowser
parameter_list|(
annotation|@
name|Nullable
name|String
name|userAgent
parameter_list|)
block|{
if|if
condition|(
name|userAgent
operator|==
literal|null
condition|)
block|{
return|return
literal|false
return|;
block|}
comment|// chrome, safari, firefox, ie
if|if
condition|(
name|userAgent
operator|.
name|startsWith
argument_list|(
literal|"Mozilla"
argument_list|)
condition|)
block|{
return|return
literal|true
return|;
block|}
return|return
literal|false
return|;
block|}
DECL|method|decodeQueryString
specifier|public
specifier|static
name|void
name|decodeQueryString
parameter_list|(
name|String
name|s
parameter_list|,
name|int
name|fromIndex
parameter_list|,
name|Map
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
name|params
parameter_list|)
block|{
if|if
condition|(
name|fromIndex
operator|<
literal|0
condition|)
block|{
return|return;
block|}
if|if
condition|(
name|fromIndex
operator|>=
name|s
operator|.
name|length
argument_list|()
condition|)
block|{
return|return;
block|}
name|int
name|queryStringLength
init|=
name|s
operator|.
name|contains
argument_list|(
literal|"#"
argument_list|)
condition|?
name|s
operator|.
name|indexOf
argument_list|(
literal|"#"
argument_list|)
else|:
name|s
operator|.
name|length
argument_list|()
decl_stmt|;
name|String
name|name
init|=
literal|null
decl_stmt|;
name|int
name|pos
init|=
name|fromIndex
decl_stmt|;
comment|// Beginning of the unprocessed region
name|int
name|i
decl_stmt|;
comment|// End of the unprocessed region
name|char
name|c
init|=
literal|0
decl_stmt|;
comment|// Current character
for|for
control|(
name|i
operator|=
name|fromIndex
init|;
name|i
operator|<
name|queryStringLength
condition|;
name|i
operator|++
control|)
block|{
name|c
operator|=
name|s
operator|.
name|charAt
argument_list|(
name|i
argument_list|)
expr_stmt|;
if|if
condition|(
name|c
operator|==
literal|'='
operator|&&
name|name
operator|==
literal|null
condition|)
block|{
if|if
condition|(
name|pos
operator|!=
name|i
condition|)
block|{
name|name
operator|=
name|decodeComponent
argument_list|(
name|s
operator|.
name|substring
argument_list|(
name|pos
argument_list|,
name|i
argument_list|)
argument_list|)
expr_stmt|;
block|}
name|pos
operator|=
name|i
operator|+
literal|1
expr_stmt|;
block|}
elseif|else
if|if
condition|(
name|c
operator|==
literal|'&'
condition|)
block|{
if|if
condition|(
name|name
operator|==
literal|null
operator|&&
name|pos
operator|!=
name|i
condition|)
block|{
comment|// We haven't seen an `=' so far but moved forward.
comment|// Must be a param of the form '&a&' so add it with
comment|// an empty value.
name|addParam
argument_list|(
name|params
argument_list|,
name|decodeComponent
argument_list|(
name|s
operator|.
name|substring
argument_list|(
name|pos
argument_list|,
name|i
argument_list|)
argument_list|)
argument_list|,
literal|""
argument_list|)
expr_stmt|;
block|}
elseif|else
if|if
condition|(
name|name
operator|!=
literal|null
condition|)
block|{
name|addParam
argument_list|(
name|params
argument_list|,
name|name
argument_list|,
name|decodeComponent
argument_list|(
name|s
operator|.
name|substring
argument_list|(
name|pos
argument_list|,
name|i
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
name|name
operator|=
literal|null
expr_stmt|;
block|}
name|pos
operator|=
name|i
operator|+
literal|1
expr_stmt|;
block|}
block|}
if|if
condition|(
name|pos
operator|!=
name|i
condition|)
block|{
comment|// Are there characters we haven't dealt with?
if|if
condition|(
name|name
operator|==
literal|null
condition|)
block|{
comment|// Yes and we haven't seen any `='.
name|addParam
argument_list|(
name|params
argument_list|,
name|decodeComponent
argument_list|(
name|s
operator|.
name|substring
argument_list|(
name|pos
argument_list|,
name|i
argument_list|)
argument_list|)
argument_list|,
literal|""
argument_list|)
expr_stmt|;
block|}
else|else
block|{
comment|// Yes and this must be the last value.
name|addParam
argument_list|(
name|params
argument_list|,
name|name
argument_list|,
name|decodeComponent
argument_list|(
name|s
operator|.
name|substring
argument_list|(
name|pos
argument_list|,
name|i
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
elseif|else
if|if
condition|(
name|name
operator|!=
literal|null
condition|)
block|{
comment|// Have we seen a name without value?
name|addParam
argument_list|(
name|params
argument_list|,
name|name
argument_list|,
literal|""
argument_list|)
expr_stmt|;
block|}
block|}
DECL|method|addParam
specifier|private
specifier|static
name|void
name|addParam
parameter_list|(
name|Map
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
name|params
parameter_list|,
name|String
name|name
parameter_list|,
name|String
name|value
parameter_list|)
block|{
name|params
operator|.
name|put
argument_list|(
name|name
argument_list|,
name|value
argument_list|)
expr_stmt|;
block|}
comment|/**      * Decodes a bit of an URL encoded by a browser.      *<p>      * This is equivalent to calling {@link #decodeComponent(String, Charset)}      * with the UTF-8 charset (recommended to comply with RFC 3986, Section 2).      *      * @param s The string to decode (can be empty).      * @return The decoded string, or {@code s} if there's nothing to decode.      *         If the string to decode is {@code null}, returns an empty string.      * @throws IllegalArgumentException if the string contains a malformed      *                                  escape sequence.      */
DECL|method|decodeComponent
specifier|public
specifier|static
name|String
name|decodeComponent
parameter_list|(
specifier|final
name|String
name|s
parameter_list|)
block|{
return|return
name|decodeComponent
argument_list|(
name|s
argument_list|,
name|StandardCharsets
operator|.
name|UTF_8
argument_list|)
return|;
block|}
comment|/**      * Decodes a bit of an URL encoded by a browser.      *<p>      * The string is expected to be encoded as per RFC 3986, Section 2.      * This is the encoding used by JavaScript functions {@code encodeURI}      * and {@code encodeURIComponent}, but not {@code escape}.  For example      * in this encoding,&eacute; (in Unicode {@code U+00E9} or in UTF-8      * {@code 0xC3 0xA9}) is encoded as {@code %C3%A9} or {@code %c3%a9}.      *<p>      * This is essentially equivalent to calling      *<code>{@link java.net.URLDecoder URLDecoder}.{@link      * java.net.URLDecoder#decode(String, String)}</code>      * except that it's over 2x faster and generates less garbage for the GC.      * Actually this function doesn't allocate any memory if there's nothing      * to decode, the argument itself is returned.      *      * @param s       The string to decode (can be empty).      * @param charset The charset to use to decode the string (should really      *                be {@link StandardCharsets#UTF_8}.      * @return The decoded string, or {@code s} if there's nothing to decode.      *         If the string to decode is {@code null}, returns an empty string.      * @throws IllegalArgumentException if the string contains a malformed      *                                  escape sequence.      */
annotation|@
name|SuppressWarnings
argument_list|(
literal|"fallthrough"
argument_list|)
DECL|method|decodeComponent
specifier|public
specifier|static
name|String
name|decodeComponent
parameter_list|(
specifier|final
name|String
name|s
parameter_list|,
specifier|final
name|Charset
name|charset
parameter_list|)
block|{
if|if
condition|(
name|s
operator|==
literal|null
condition|)
block|{
return|return
literal|""
return|;
block|}
specifier|final
name|int
name|size
init|=
name|s
operator|.
name|length
argument_list|()
decl_stmt|;
name|boolean
name|modified
init|=
literal|false
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
name|size
condition|;
name|i
operator|++
control|)
block|{
specifier|final
name|char
name|c
init|=
name|s
operator|.
name|charAt
argument_list|(
name|i
argument_list|)
decl_stmt|;
switch|switch
condition|(
name|c
condition|)
block|{
case|case
literal|'%'
case|:
name|i
operator|++
expr_stmt|;
comment|// We can skip at least one char, e.g. `%%'.
comment|// Fall through.
case|case
literal|'+'
case|:
name|modified
operator|=
literal|true
expr_stmt|;
break|break;
block|}
block|}
if|if
condition|(
operator|!
name|modified
condition|)
block|{
return|return
name|s
return|;
block|}
specifier|final
name|byte
index|[]
name|buf
init|=
operator|new
name|byte
index|[
name|size
index|]
decl_stmt|;
name|int
name|pos
init|=
literal|0
decl_stmt|;
comment|// position in `buf'.
for|for
control|(
name|int
name|i
init|=
literal|0
init|;
name|i
operator|<
name|size
condition|;
name|i
operator|++
control|)
block|{
name|char
name|c
init|=
name|s
operator|.
name|charAt
argument_list|(
name|i
argument_list|)
decl_stmt|;
switch|switch
condition|(
name|c
condition|)
block|{
case|case
literal|'+'
case|:
name|buf
index|[
name|pos
operator|++
index|]
operator|=
literal|' '
expr_stmt|;
comment|// "+" -> " "
break|break;
case|case
literal|'%'
case|:
if|if
condition|(
name|i
operator|==
name|size
operator|-
literal|1
condition|)
block|{
throw|throw
operator|new
name|IllegalArgumentException
argument_list|(
literal|"unterminated escape"
operator|+
literal|" sequence at end of string: "
operator|+
name|s
argument_list|)
throw|;
block|}
name|c
operator|=
name|s
operator|.
name|charAt
argument_list|(
operator|++
name|i
argument_list|)
expr_stmt|;
if|if
condition|(
name|c
operator|==
literal|'%'
condition|)
block|{
name|buf
index|[
name|pos
operator|++
index|]
operator|=
literal|'%'
expr_stmt|;
comment|// "%%" -> "%"
break|break;
block|}
elseif|else
if|if
condition|(
name|i
operator|==
name|size
operator|-
literal|1
condition|)
block|{
throw|throw
operator|new
name|IllegalArgumentException
argument_list|(
literal|"partial escape"
operator|+
literal|" sequence at end of string: "
operator|+
name|s
argument_list|)
throw|;
block|}
name|c
operator|=
name|decodeHexNibble
argument_list|(
name|c
argument_list|)
expr_stmt|;
specifier|final
name|char
name|c2
init|=
name|decodeHexNibble
argument_list|(
name|s
operator|.
name|charAt
argument_list|(
operator|++
name|i
argument_list|)
argument_list|)
decl_stmt|;
if|if
condition|(
name|c
operator|==
name|Character
operator|.
name|MAX_VALUE
operator|||
name|c2
operator|==
name|Character
operator|.
name|MAX_VALUE
condition|)
block|{
throw|throw
operator|new
name|IllegalArgumentException
argument_list|(
literal|"invalid escape sequence `%"
operator|+
name|s
operator|.
name|charAt
argument_list|(
name|i
operator|-
literal|1
argument_list|)
operator|+
name|s
operator|.
name|charAt
argument_list|(
name|i
argument_list|)
operator|+
literal|"' at index "
operator|+
operator|(
name|i
operator|-
literal|2
operator|)
operator|+
literal|" of: "
operator|+
name|s
argument_list|)
throw|;
block|}
name|c
operator|=
call|(
name|char
call|)
argument_list|(
name|c
operator|*
literal|16
operator|+
name|c2
argument_list|)
expr_stmt|;
comment|// Fall through.
default|default:
name|buf
index|[
name|pos
operator|++
index|]
operator|=
operator|(
name|byte
operator|)
name|c
expr_stmt|;
break|break;
block|}
block|}
return|return
operator|new
name|String
argument_list|(
name|buf
argument_list|,
literal|0
argument_list|,
name|pos
argument_list|,
name|charset
argument_list|)
return|;
block|}
comment|/**      * Helper to decode half of a hexadecimal number from a string.      *      * @param c The ASCII character of the hexadecimal number to decode.      *          Must be in the range {@code [0-9a-fA-F]}.      * @return The hexadecimal value represented in the ASCII character      *         given, or {@link Character#MAX_VALUE} if the character is invalid.      */
DECL|method|decodeHexNibble
specifier|private
specifier|static
name|char
name|decodeHexNibble
parameter_list|(
specifier|final
name|char
name|c
parameter_list|)
block|{
if|if
condition|(
literal|'0'
operator|<=
name|c
operator|&&
name|c
operator|<=
literal|'9'
condition|)
block|{
return|return
call|(
name|char
call|)
argument_list|(
name|c
operator|-
literal|'0'
argument_list|)
return|;
block|}
elseif|else
if|if
condition|(
literal|'a'
operator|<=
name|c
operator|&&
name|c
operator|<=
literal|'f'
condition|)
block|{
return|return
call|(
name|char
call|)
argument_list|(
name|c
operator|-
literal|'a'
operator|+
literal|10
argument_list|)
return|;
block|}
elseif|else
if|if
condition|(
literal|'A'
operator|<=
name|c
operator|&&
name|c
operator|<=
literal|'F'
condition|)
block|{
return|return
call|(
name|char
call|)
argument_list|(
name|c
operator|-
literal|'A'
operator|+
literal|10
argument_list|)
return|;
block|}
else|else
block|{
return|return
name|Character
operator|.
name|MAX_VALUE
return|;
block|}
block|}
comment|/**      * Determine if CORS setting is a regex      *      * @return a corresponding {@link Pattern} if so and o.w. null.      */
DECL|method|checkCorsSettingForRegex
specifier|public
specifier|static
name|Pattern
name|checkCorsSettingForRegex
parameter_list|(
name|String
name|corsSetting
parameter_list|)
block|{
if|if
condition|(
name|corsSetting
operator|==
literal|null
condition|)
block|{
return|return
literal|null
return|;
block|}
name|int
name|len
init|=
name|corsSetting
operator|.
name|length
argument_list|()
decl_stmt|;
name|boolean
name|isRegex
init|=
name|len
operator|>
literal|2
operator|&&
name|corsSetting
operator|.
name|startsWith
argument_list|(
literal|"/"
argument_list|)
operator|&&
name|corsSetting
operator|.
name|endsWith
argument_list|(
literal|"/"
argument_list|)
decl_stmt|;
if|if
condition|(
name|isRegex
condition|)
block|{
return|return
name|Pattern
operator|.
name|compile
argument_list|(
name|corsSetting
operator|.
name|substring
argument_list|(
literal|1
argument_list|,
name|corsSetting
operator|.
name|length
argument_list|()
operator|-
literal|1
argument_list|)
argument_list|)
return|;
block|}
return|return
literal|null
return|;
block|}
block|}
end_class

end_unit

