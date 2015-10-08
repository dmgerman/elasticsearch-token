begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to Elasticsearch under one or more contributor  * license agreements. See the NOTICE file distributed with  * this work for additional information regarding copyright  * ownership. Elasticsearch licenses this file to you under  * the Apache License, Version 2.0 (the "License"); you may  * not use this file except in compliance with the License.  * You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.index.codec.postingsformat
package|package
name|org
operator|.
name|elasticsearch
operator|.
name|index
operator|.
name|codec
operator|.
name|postingsformat
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
name|FieldsConsumer
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
name|FieldsProducer
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
name|lucene50
operator|.
name|Lucene50PostingsFormat
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
name|index
operator|.
name|SegmentReadState
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
name|index
operator|.
name|SegmentWriteState
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
name|Lucene
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
name|util
operator|.
name|BloomFilter
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
name|internal
operator|.
name|UidFieldMapper
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
name|function
operator|.
name|Predicate
import|;
end_import

begin_comment
comment|/**  * This is the old default postings format for Elasticsearch that special cases  * the<tt>_uid</tt> field to use a bloom filter while all other fields  * will use a {@link Lucene50PostingsFormat}. This format will reuse the underlying  * {@link Lucene50PostingsFormat} and its files also for the<tt>_uid</tt> saving up to  * 5 files per segment in the default case.  *<p>  * @deprecated only for reading old segments  */
end_comment

begin_class
annotation|@
name|Deprecated
DECL|class|Elasticsearch090PostingsFormat
specifier|public
class|class
name|Elasticsearch090PostingsFormat
extends|extends
name|PostingsFormat
block|{
DECL|field|bloomPostings
specifier|protected
specifier|final
name|BloomFilterPostingsFormat
name|bloomPostings
decl_stmt|;
DECL|method|Elasticsearch090PostingsFormat
specifier|public
name|Elasticsearch090PostingsFormat
parameter_list|()
block|{
name|super
argument_list|(
literal|"es090"
argument_list|)
expr_stmt|;
name|Lucene50PostingsFormat
name|delegate
init|=
operator|new
name|Lucene50PostingsFormat
argument_list|()
decl_stmt|;
assert|assert
name|delegate
operator|.
name|getName
argument_list|()
operator|.
name|equals
argument_list|(
name|Lucene
operator|.
name|LATEST_POSTINGS_FORMAT
argument_list|)
assert|;
name|bloomPostings
operator|=
operator|new
name|BloomFilterPostingsFormat
argument_list|(
name|delegate
argument_list|,
name|BloomFilter
operator|.
name|Factory
operator|.
name|DEFAULT
argument_list|)
expr_stmt|;
block|}
DECL|method|getDefaultWrapped
specifier|public
name|PostingsFormat
name|getDefaultWrapped
parameter_list|()
block|{
return|return
name|bloomPostings
operator|.
name|getDelegate
argument_list|()
return|;
block|}
DECL|field|UID_FIELD_FILTER
specifier|protected
specifier|static
specifier|final
name|Predicate
argument_list|<
name|String
argument_list|>
name|UID_FIELD_FILTER
init|=
name|field
lambda|->
name|UidFieldMapper
operator|.
name|NAME
operator|.
name|equals
argument_list|(
name|field
argument_list|)
decl_stmt|;
annotation|@
name|Override
DECL|method|fieldsConsumer
specifier|public
name|FieldsConsumer
name|fieldsConsumer
parameter_list|(
name|SegmentWriteState
name|state
parameter_list|)
throws|throws
name|IOException
block|{
throw|throw
operator|new
name|UnsupportedOperationException
argument_list|(
literal|"this codec can only be used for reading"
argument_list|)
throw|;
block|}
annotation|@
name|Override
DECL|method|fieldsProducer
specifier|public
name|FieldsProducer
name|fieldsProducer
parameter_list|(
name|SegmentReadState
name|state
parameter_list|)
throws|throws
name|IOException
block|{
comment|// we can just return the delegate here since we didn't record bloom filters for
comment|// the other fields.
return|return
name|bloomPostings
operator|.
name|fieldsProducer
argument_list|(
name|state
argument_list|)
return|;
block|}
block|}
end_class

end_unit

