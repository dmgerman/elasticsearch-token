begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to ElasticSearch and Shay Banon under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership. ElasticSearch licenses this  * file to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.index.codec
package|package
name|org
operator|.
name|elasticsearch
operator|.
name|index
operator|.
name|codec
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
name|codecs
operator|.
name|PostingsFormat
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
name|codecs
operator|.
name|lucene40
operator|.
name|Lucene40Codec
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
name|codecs
operator|.
name|lucene41
operator|.
name|Lucene41Codec
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
name|codec
operator|.
name|postingsformat
operator|.
name|PostingsFormatProvider
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
name|MapperService
import|;
end_import

begin_comment
comment|/**  * {@link PerFieldMappingPostingFormatCodec This postings format} is the default  * {@link PostingsFormat} for Elasticsearch. It utilizes the  * {@link MapperService} to lookup a {@link PostingsFormat} per field. This  * allows users to change the low level postings format for individual fields  * per index in real time via the mapping API. If no specific postings format is  * configured for a specific field the default postings format is used.  */
end_comment

begin_comment
comment|// LUCENE UPGRADE: make sure to move to a new codec depending on the lucene version
end_comment

begin_class
DECL|class|PerFieldMappingPostingFormatCodec
specifier|public
class|class
name|PerFieldMappingPostingFormatCodec
extends|extends
name|Lucene41Codec
block|{
DECL|field|mapperService
specifier|private
specifier|final
name|MapperService
name|mapperService
decl_stmt|;
DECL|field|defaultPostingFormat
specifier|private
specifier|final
name|PostingsFormat
name|defaultPostingFormat
decl_stmt|;
DECL|method|PerFieldMappingPostingFormatCodec
specifier|public
name|PerFieldMappingPostingFormatCodec
parameter_list|(
name|MapperService
name|mapperService
parameter_list|,
name|PostingsFormat
name|defaultPostingFormat
parameter_list|)
block|{
name|this
operator|.
name|mapperService
operator|=
name|mapperService
expr_stmt|;
name|this
operator|.
name|defaultPostingFormat
operator|=
name|defaultPostingFormat
expr_stmt|;
block|}
annotation|@
name|Override
DECL|method|getPostingsFormatForField
specifier|public
name|PostingsFormat
name|getPostingsFormatForField
parameter_list|(
name|String
name|field
parameter_list|)
block|{
name|PostingsFormatProvider
name|postingsFormat
init|=
name|mapperService
operator|.
name|indexName
argument_list|(
name|field
argument_list|)
operator|.
name|mapper
argument_list|()
operator|.
name|postingsFormatProvider
argument_list|()
decl_stmt|;
return|return
name|postingsFormat
operator|!=
literal|null
condition|?
name|postingsFormat
operator|.
name|get
argument_list|()
else|:
name|defaultPostingFormat
return|;
block|}
block|}
end_class

end_unit

