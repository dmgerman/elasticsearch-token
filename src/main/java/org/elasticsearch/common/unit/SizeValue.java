begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to Elasticsearch under one or more contributor  * license agreements. See the NOTICE file distributed with  * this work for additional information regarding copyright  * ownership. Elasticsearch licenses this file to you under  * the Apache License, Version 2.0 (the "License"); you may  * not use this file except in compliance with the License.  * You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.common.unit
package|package
name|org
operator|.
name|elasticsearch
operator|.
name|common
operator|.
name|unit
package|;
end_package

begin_import
import|import
name|com
operator|.
name|google
operator|.
name|common
operator|.
name|base
operator|.
name|Preconditions
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|ElasticsearchParseException
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
name|io
operator|.
name|stream
operator|.
name|Streamable
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
name|io
operator|.
name|Serializable
import|;
end_import

begin_comment
comment|/**  *  */
end_comment

begin_class
DECL|class|SizeValue
specifier|public
class|class
name|SizeValue
implements|implements
name|Serializable
implements|,
name|Streamable
block|{
DECL|field|size
specifier|private
name|long
name|size
decl_stmt|;
DECL|field|sizeUnit
specifier|private
name|SizeUnit
name|sizeUnit
decl_stmt|;
DECL|method|SizeValue
specifier|private
name|SizeValue
parameter_list|()
block|{      }
DECL|method|SizeValue
specifier|public
name|SizeValue
parameter_list|(
name|long
name|singles
parameter_list|)
block|{
name|this
argument_list|(
name|singles
argument_list|,
name|SizeUnit
operator|.
name|SINGLE
argument_list|)
expr_stmt|;
block|}
DECL|method|SizeValue
specifier|public
name|SizeValue
parameter_list|(
name|long
name|size
parameter_list|,
name|SizeUnit
name|sizeUnit
parameter_list|)
block|{
name|Preconditions
operator|.
name|checkArgument
argument_list|(
name|size
operator|>=
literal|0
argument_list|,
literal|"size in SizeValue may not be negative"
argument_list|)
expr_stmt|;
name|this
operator|.
name|size
operator|=
name|size
expr_stmt|;
name|this
operator|.
name|sizeUnit
operator|=
name|sizeUnit
expr_stmt|;
block|}
DECL|method|singles
specifier|public
name|long
name|singles
parameter_list|()
block|{
return|return
name|sizeUnit
operator|.
name|toSingles
argument_list|(
name|size
argument_list|)
return|;
block|}
DECL|method|getSingles
specifier|public
name|long
name|getSingles
parameter_list|()
block|{
return|return
name|singles
argument_list|()
return|;
block|}
DECL|method|kilo
specifier|public
name|long
name|kilo
parameter_list|()
block|{
return|return
name|sizeUnit
operator|.
name|toKilo
argument_list|(
name|size
argument_list|)
return|;
block|}
DECL|method|getKilo
specifier|public
name|long
name|getKilo
parameter_list|()
block|{
return|return
name|kilo
argument_list|()
return|;
block|}
DECL|method|mega
specifier|public
name|long
name|mega
parameter_list|()
block|{
return|return
name|sizeUnit
operator|.
name|toMega
argument_list|(
name|size
argument_list|)
return|;
block|}
DECL|method|getMega
specifier|public
name|long
name|getMega
parameter_list|()
block|{
return|return
name|mega
argument_list|()
return|;
block|}
DECL|method|giga
specifier|public
name|long
name|giga
parameter_list|()
block|{
return|return
name|sizeUnit
operator|.
name|toGiga
argument_list|(
name|size
argument_list|)
return|;
block|}
DECL|method|getGiga
specifier|public
name|long
name|getGiga
parameter_list|()
block|{
return|return
name|giga
argument_list|()
return|;
block|}
DECL|method|tera
specifier|public
name|long
name|tera
parameter_list|()
block|{
return|return
name|sizeUnit
operator|.
name|toTera
argument_list|(
name|size
argument_list|)
return|;
block|}
DECL|method|getTera
specifier|public
name|long
name|getTera
parameter_list|()
block|{
return|return
name|tera
argument_list|()
return|;
block|}
DECL|method|peta
specifier|public
name|long
name|peta
parameter_list|()
block|{
return|return
name|sizeUnit
operator|.
name|toPeta
argument_list|(
name|size
argument_list|)
return|;
block|}
DECL|method|getPeta
specifier|public
name|long
name|getPeta
parameter_list|()
block|{
return|return
name|peta
argument_list|()
return|;
block|}
DECL|method|kiloFrac
specifier|public
name|double
name|kiloFrac
parameter_list|()
block|{
return|return
operator|(
operator|(
name|double
operator|)
name|singles
argument_list|()
operator|)
operator|/
name|SizeUnit
operator|.
name|C1
return|;
block|}
DECL|method|getKiloFrac
specifier|public
name|double
name|getKiloFrac
parameter_list|()
block|{
return|return
name|kiloFrac
argument_list|()
return|;
block|}
DECL|method|megaFrac
specifier|public
name|double
name|megaFrac
parameter_list|()
block|{
return|return
operator|(
operator|(
name|double
operator|)
name|singles
argument_list|()
operator|)
operator|/
name|SizeUnit
operator|.
name|C2
return|;
block|}
DECL|method|getMegaFrac
specifier|public
name|double
name|getMegaFrac
parameter_list|()
block|{
return|return
name|megaFrac
argument_list|()
return|;
block|}
DECL|method|gigaFrac
specifier|public
name|double
name|gigaFrac
parameter_list|()
block|{
return|return
operator|(
operator|(
name|double
operator|)
name|singles
argument_list|()
operator|)
operator|/
name|SizeUnit
operator|.
name|C3
return|;
block|}
DECL|method|getGigaFrac
specifier|public
name|double
name|getGigaFrac
parameter_list|()
block|{
return|return
name|gigaFrac
argument_list|()
return|;
block|}
DECL|method|teraFrac
specifier|public
name|double
name|teraFrac
parameter_list|()
block|{
return|return
operator|(
operator|(
name|double
operator|)
name|singles
argument_list|()
operator|)
operator|/
name|SizeUnit
operator|.
name|C4
return|;
block|}
DECL|method|getTeraFrac
specifier|public
name|double
name|getTeraFrac
parameter_list|()
block|{
return|return
name|teraFrac
argument_list|()
return|;
block|}
DECL|method|petaFrac
specifier|public
name|double
name|petaFrac
parameter_list|()
block|{
return|return
operator|(
operator|(
name|double
operator|)
name|singles
argument_list|()
operator|)
operator|/
name|SizeUnit
operator|.
name|C5
return|;
block|}
DECL|method|getPetaFrac
specifier|public
name|double
name|getPetaFrac
parameter_list|()
block|{
return|return
name|petaFrac
argument_list|()
return|;
block|}
annotation|@
name|Override
DECL|method|toString
specifier|public
name|String
name|toString
parameter_list|()
block|{
name|long
name|singles
init|=
name|singles
argument_list|()
decl_stmt|;
name|double
name|value
init|=
name|singles
decl_stmt|;
name|String
name|suffix
init|=
literal|""
decl_stmt|;
if|if
condition|(
name|singles
operator|>=
name|SizeUnit
operator|.
name|C5
condition|)
block|{
name|value
operator|=
name|petaFrac
argument_list|()
expr_stmt|;
name|suffix
operator|=
literal|"p"
expr_stmt|;
block|}
elseif|else
if|if
condition|(
name|singles
operator|>=
name|SizeUnit
operator|.
name|C4
condition|)
block|{
name|value
operator|=
name|teraFrac
argument_list|()
expr_stmt|;
name|suffix
operator|=
literal|"t"
expr_stmt|;
block|}
elseif|else
if|if
condition|(
name|singles
operator|>=
name|SizeUnit
operator|.
name|C3
condition|)
block|{
name|value
operator|=
name|gigaFrac
argument_list|()
expr_stmt|;
name|suffix
operator|=
literal|"g"
expr_stmt|;
block|}
elseif|else
if|if
condition|(
name|singles
operator|>=
name|SizeUnit
operator|.
name|C2
condition|)
block|{
name|value
operator|=
name|megaFrac
argument_list|()
expr_stmt|;
name|suffix
operator|=
literal|"m"
expr_stmt|;
block|}
elseif|else
if|if
condition|(
name|singles
operator|>=
name|SizeUnit
operator|.
name|C1
condition|)
block|{
name|value
operator|=
name|kiloFrac
argument_list|()
expr_stmt|;
name|suffix
operator|=
literal|"k"
expr_stmt|;
block|}
return|return
name|Strings
operator|.
name|format1Decimals
argument_list|(
name|value
argument_list|,
name|suffix
argument_list|)
return|;
block|}
DECL|method|parseSizeValue
specifier|public
specifier|static
name|SizeValue
name|parseSizeValue
parameter_list|(
name|String
name|sValue
parameter_list|)
throws|throws
name|ElasticsearchParseException
block|{
return|return
name|parseSizeValue
argument_list|(
name|sValue
argument_list|,
literal|null
argument_list|)
return|;
block|}
DECL|method|parseSizeValue
specifier|public
specifier|static
name|SizeValue
name|parseSizeValue
parameter_list|(
name|String
name|sValue
parameter_list|,
name|SizeValue
name|defaultValue
parameter_list|)
throws|throws
name|ElasticsearchParseException
block|{
if|if
condition|(
name|sValue
operator|==
literal|null
condition|)
block|{
return|return
name|defaultValue
return|;
block|}
name|long
name|singles
decl_stmt|;
try|try
block|{
if|if
condition|(
name|sValue
operator|.
name|endsWith
argument_list|(
literal|"b"
argument_list|)
condition|)
block|{
name|singles
operator|=
name|Long
operator|.
name|parseLong
argument_list|(
name|sValue
operator|.
name|substring
argument_list|(
literal|0
argument_list|,
name|sValue
operator|.
name|length
argument_list|()
operator|-
literal|1
argument_list|)
argument_list|)
expr_stmt|;
block|}
elseif|else
if|if
condition|(
name|sValue
operator|.
name|endsWith
argument_list|(
literal|"k"
argument_list|)
operator|||
name|sValue
operator|.
name|endsWith
argument_list|(
literal|"K"
argument_list|)
condition|)
block|{
name|singles
operator|=
call|(
name|long
call|)
argument_list|(
name|Double
operator|.
name|parseDouble
argument_list|(
name|sValue
operator|.
name|substring
argument_list|(
literal|0
argument_list|,
name|sValue
operator|.
name|length
argument_list|()
operator|-
literal|1
argument_list|)
argument_list|)
operator|*
name|SizeUnit
operator|.
name|C1
argument_list|)
expr_stmt|;
block|}
elseif|else
if|if
condition|(
name|sValue
operator|.
name|endsWith
argument_list|(
literal|"m"
argument_list|)
operator|||
name|sValue
operator|.
name|endsWith
argument_list|(
literal|"M"
argument_list|)
condition|)
block|{
name|singles
operator|=
call|(
name|long
call|)
argument_list|(
name|Double
operator|.
name|parseDouble
argument_list|(
name|sValue
operator|.
name|substring
argument_list|(
literal|0
argument_list|,
name|sValue
operator|.
name|length
argument_list|()
operator|-
literal|1
argument_list|)
argument_list|)
operator|*
name|SizeUnit
operator|.
name|C2
argument_list|)
expr_stmt|;
block|}
elseif|else
if|if
condition|(
name|sValue
operator|.
name|endsWith
argument_list|(
literal|"g"
argument_list|)
operator|||
name|sValue
operator|.
name|endsWith
argument_list|(
literal|"G"
argument_list|)
condition|)
block|{
name|singles
operator|=
call|(
name|long
call|)
argument_list|(
name|Double
operator|.
name|parseDouble
argument_list|(
name|sValue
operator|.
name|substring
argument_list|(
literal|0
argument_list|,
name|sValue
operator|.
name|length
argument_list|()
operator|-
literal|1
argument_list|)
argument_list|)
operator|*
name|SizeUnit
operator|.
name|C3
argument_list|)
expr_stmt|;
block|}
elseif|else
if|if
condition|(
name|sValue
operator|.
name|endsWith
argument_list|(
literal|"t"
argument_list|)
operator|||
name|sValue
operator|.
name|endsWith
argument_list|(
literal|"T"
argument_list|)
condition|)
block|{
name|singles
operator|=
call|(
name|long
call|)
argument_list|(
name|Double
operator|.
name|parseDouble
argument_list|(
name|sValue
operator|.
name|substring
argument_list|(
literal|0
argument_list|,
name|sValue
operator|.
name|length
argument_list|()
operator|-
literal|1
argument_list|)
argument_list|)
operator|*
name|SizeUnit
operator|.
name|C4
argument_list|)
expr_stmt|;
block|}
elseif|else
if|if
condition|(
name|sValue
operator|.
name|endsWith
argument_list|(
literal|"p"
argument_list|)
operator|||
name|sValue
operator|.
name|endsWith
argument_list|(
literal|"P"
argument_list|)
condition|)
block|{
name|singles
operator|=
call|(
name|long
call|)
argument_list|(
name|Double
operator|.
name|parseDouble
argument_list|(
name|sValue
operator|.
name|substring
argument_list|(
literal|0
argument_list|,
name|sValue
operator|.
name|length
argument_list|()
operator|-
literal|1
argument_list|)
argument_list|)
operator|*
name|SizeUnit
operator|.
name|C5
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|singles
operator|=
name|Long
operator|.
name|parseLong
argument_list|(
name|sValue
argument_list|)
expr_stmt|;
block|}
block|}
catch|catch
parameter_list|(
name|NumberFormatException
name|e
parameter_list|)
block|{
throw|throw
operator|new
name|ElasticsearchParseException
argument_list|(
literal|"Failed to parse ["
operator|+
name|sValue
operator|+
literal|"]"
argument_list|,
name|e
argument_list|)
throw|;
block|}
return|return
operator|new
name|SizeValue
argument_list|(
name|singles
argument_list|,
name|SizeUnit
operator|.
name|SINGLE
argument_list|)
return|;
block|}
DECL|method|readSizeValue
specifier|public
specifier|static
name|SizeValue
name|readSizeValue
parameter_list|(
name|StreamInput
name|in
parameter_list|)
throws|throws
name|IOException
block|{
name|SizeValue
name|sizeValue
init|=
operator|new
name|SizeValue
argument_list|()
decl_stmt|;
name|sizeValue
operator|.
name|readFrom
argument_list|(
name|in
argument_list|)
expr_stmt|;
return|return
name|sizeValue
return|;
block|}
annotation|@
name|Override
DECL|method|readFrom
specifier|public
name|void
name|readFrom
parameter_list|(
name|StreamInput
name|in
parameter_list|)
throws|throws
name|IOException
block|{
name|size
operator|=
name|in
operator|.
name|readVLong
argument_list|()
expr_stmt|;
name|sizeUnit
operator|=
name|SizeUnit
operator|.
name|SINGLE
expr_stmt|;
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
name|writeVLong
argument_list|(
name|singles
argument_list|()
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
DECL|method|equals
specifier|public
name|boolean
name|equals
parameter_list|(
name|Object
name|o
parameter_list|)
block|{
if|if
condition|(
name|this
operator|==
name|o
condition|)
return|return
literal|true
return|;
if|if
condition|(
name|o
operator|==
literal|null
operator|||
name|getClass
argument_list|()
operator|!=
name|o
operator|.
name|getClass
argument_list|()
condition|)
return|return
literal|false
return|;
name|SizeValue
name|sizeValue
init|=
operator|(
name|SizeValue
operator|)
name|o
decl_stmt|;
if|if
condition|(
name|size
operator|!=
name|sizeValue
operator|.
name|size
condition|)
return|return
literal|false
return|;
if|if
condition|(
name|sizeUnit
operator|!=
name|sizeValue
operator|.
name|sizeUnit
condition|)
return|return
literal|false
return|;
return|return
literal|true
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
name|int
name|result
init|=
call|(
name|int
call|)
argument_list|(
name|size
operator|^
operator|(
name|size
operator|>>>
literal|32
operator|)
argument_list|)
decl_stmt|;
name|result
operator|=
literal|31
operator|*
name|result
operator|+
operator|(
name|sizeUnit
operator|!=
literal|null
condition|?
name|sizeUnit
operator|.
name|hashCode
argument_list|()
else|:
literal|0
operator|)
expr_stmt|;
return|return
name|result
return|;
block|}
block|}
end_class

end_unit

