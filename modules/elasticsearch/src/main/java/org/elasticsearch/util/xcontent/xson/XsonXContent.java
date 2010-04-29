begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to Elastic Search and Shay Banon under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership. Elastic Search licenses this  * file to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.util.xcontent.xson
package|package
name|org
operator|.
name|elasticsearch
operator|.
name|util
operator|.
name|xcontent
operator|.
name|xson
package|;
end_package

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|ElasticSearchException
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|ElasticSearchIllegalStateException
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|util
operator|.
name|ThreadLocals
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|util
operator|.
name|io
operator|.
name|FastByteArrayInputStream
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|util
operator|.
name|xcontent
operator|.
name|XContent
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|util
operator|.
name|xcontent
operator|.
name|XContentGenerator
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|util
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
name|util
operator|.
name|xcontent
operator|.
name|XContentType
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|util
operator|.
name|xcontent
operator|.
name|builder
operator|.
name|BinaryXContentBuilder
import|;
end_import

begin_import
import|import
name|java
operator|.
name|io
operator|.
name|*
import|;
end_import

begin_comment
comment|/**  * A binary representation of content (basically, JSON encoded in optimized binary format).  *  * @author kimchy (shay.banon)  */
end_comment

begin_class
DECL|class|XsonXContent
specifier|public
class|class
name|XsonXContent
implements|implements
name|XContent
block|{
DECL|class|CachedBinaryBuilder
specifier|public
specifier|static
class|class
name|CachedBinaryBuilder
block|{
DECL|field|cache
specifier|private
specifier|static
specifier|final
name|ThreadLocal
argument_list|<
name|ThreadLocals
operator|.
name|CleanableValue
argument_list|<
name|BinaryXContentBuilder
argument_list|>
argument_list|>
name|cache
init|=
operator|new
name|ThreadLocal
argument_list|<
name|ThreadLocals
operator|.
name|CleanableValue
argument_list|<
name|BinaryXContentBuilder
argument_list|>
argument_list|>
argument_list|()
block|{
annotation|@
name|Override
specifier|protected
name|ThreadLocals
operator|.
name|CleanableValue
argument_list|<
name|BinaryXContentBuilder
argument_list|>
name|initialValue
parameter_list|()
block|{
try|try
block|{
name|BinaryXContentBuilder
name|builder
init|=
operator|new
name|BinaryXContentBuilder
argument_list|(
operator|new
name|XsonXContent
argument_list|()
argument_list|)
decl_stmt|;
return|return
operator|new
name|ThreadLocals
operator|.
name|CleanableValue
argument_list|<
name|BinaryXContentBuilder
argument_list|>
argument_list|(
name|builder
argument_list|)
return|;
block|}
catch|catch
parameter_list|(
name|IOException
name|e
parameter_list|)
block|{
throw|throw
operator|new
name|ElasticSearchException
argument_list|(
literal|"Failed to create xson generator"
argument_list|,
name|e
argument_list|)
throw|;
block|}
block|}
block|}
decl_stmt|;
comment|/**          * Returns the cached thread local generator, with its internal {@link StringBuilder} cleared.          */
DECL|method|cached
specifier|static
name|BinaryXContentBuilder
name|cached
parameter_list|()
throws|throws
name|IOException
block|{
name|ThreadLocals
operator|.
name|CleanableValue
argument_list|<
name|BinaryXContentBuilder
argument_list|>
name|cached
init|=
name|cache
operator|.
name|get
argument_list|()
decl_stmt|;
name|cached
operator|.
name|get
argument_list|()
operator|.
name|reset
argument_list|()
expr_stmt|;
return|return
name|cached
operator|.
name|get
argument_list|()
return|;
block|}
block|}
DECL|method|contentBinaryBuilder
specifier|public
specifier|static
name|BinaryXContentBuilder
name|contentBinaryBuilder
parameter_list|()
throws|throws
name|IOException
block|{
return|return
name|CachedBinaryBuilder
operator|.
name|cached
argument_list|()
return|;
block|}
DECL|method|type
annotation|@
name|Override
specifier|public
name|XContentType
name|type
parameter_list|()
block|{
return|return
name|XContentType
operator|.
name|XSON
return|;
block|}
DECL|method|createGenerator
annotation|@
name|Override
specifier|public
name|XContentGenerator
name|createGenerator
parameter_list|(
name|OutputStream
name|os
parameter_list|)
throws|throws
name|IOException
block|{
return|return
operator|new
name|XsonXContentGenerator
argument_list|(
name|os
argument_list|)
return|;
block|}
DECL|method|createGenerator
annotation|@
name|Override
specifier|public
name|XContentGenerator
name|createGenerator
parameter_list|(
name|Writer
name|writer
parameter_list|)
throws|throws
name|IOException
block|{
throw|throw
operator|new
name|ElasticSearchIllegalStateException
argument_list|(
literal|"Can't create generator over xson with textual data"
argument_list|)
throw|;
block|}
DECL|method|createParser
annotation|@
name|Override
specifier|public
name|XContentParser
name|createParser
parameter_list|(
name|String
name|content
parameter_list|)
throws|throws
name|IOException
block|{
throw|throw
operator|new
name|ElasticSearchIllegalStateException
argument_list|(
literal|"Can't create parser over xson for textual data"
argument_list|)
throw|;
block|}
DECL|method|createParser
annotation|@
name|Override
specifier|public
name|XContentParser
name|createParser
parameter_list|(
name|InputStream
name|is
parameter_list|)
throws|throws
name|IOException
block|{
return|return
operator|new
name|XsonXContentParser
argument_list|(
name|is
argument_list|)
return|;
block|}
DECL|method|createParser
annotation|@
name|Override
specifier|public
name|XContentParser
name|createParser
parameter_list|(
name|byte
index|[]
name|data
parameter_list|)
throws|throws
name|IOException
block|{
return|return
operator|new
name|XsonXContentParser
argument_list|(
operator|new
name|FastByteArrayInputStream
argument_list|(
name|data
argument_list|)
argument_list|)
return|;
block|}
DECL|method|createParser
annotation|@
name|Override
specifier|public
name|XContentParser
name|createParser
parameter_list|(
name|byte
index|[]
name|data
parameter_list|,
name|int
name|offset
parameter_list|,
name|int
name|length
parameter_list|)
throws|throws
name|IOException
block|{
return|return
operator|new
name|XsonXContentParser
argument_list|(
operator|new
name|FastByteArrayInputStream
argument_list|(
name|data
argument_list|,
name|offset
argument_list|,
name|length
argument_list|)
argument_list|)
return|;
block|}
DECL|method|createParser
annotation|@
name|Override
specifier|public
name|XContentParser
name|createParser
parameter_list|(
name|Reader
name|reader
parameter_list|)
throws|throws
name|IOException
block|{
throw|throw
operator|new
name|ElasticSearchIllegalStateException
argument_list|(
literal|"Can't create parser over xson for textual data"
argument_list|)
throw|;
block|}
block|}
end_class

end_unit

