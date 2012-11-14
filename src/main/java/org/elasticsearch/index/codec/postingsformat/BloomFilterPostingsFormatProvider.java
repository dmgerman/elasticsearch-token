begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to ElasticSearch and Shay Banon under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership. ElasticSearch licenses this  * file to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
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
name|bloom
operator|.
name|BloomFilterFactory
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
name|bloom
operator|.
name|BloomFilteringPostingsFormat
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
name|bloom
operator|.
name|FuzzySet
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
name|FieldInfo
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
name|inject
operator|.
name|Inject
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
name|inject
operator|.
name|assistedinject
operator|.
name|Assisted
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
name|settings
operator|.
name|Settings
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
name|settings
operator|.
name|IndexSettings
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
comment|/**  */
end_comment

begin_class
DECL|class|BloomFilterPostingsFormatProvider
specifier|public
class|class
name|BloomFilterPostingsFormatProvider
extends|extends
name|AbstractPostingsFormatProvider
block|{
DECL|field|desiredMaxSaturation
specifier|private
specifier|final
name|float
name|desiredMaxSaturation
decl_stmt|;
DECL|field|saturationLimit
specifier|private
specifier|final
name|float
name|saturationLimit
decl_stmt|;
DECL|field|delegate
specifier|private
specifier|final
name|PostingsFormatProvider
name|delegate
decl_stmt|;
DECL|field|postingsFormat
specifier|private
specifier|final
name|BloomFilteringPostingsFormat
name|postingsFormat
decl_stmt|;
annotation|@
name|Inject
DECL|method|BloomFilterPostingsFormatProvider
specifier|public
name|BloomFilterPostingsFormatProvider
parameter_list|(
annotation|@
name|IndexSettings
name|Settings
name|indexSettings
parameter_list|,
annotation|@
name|Nullable
name|Map
argument_list|<
name|String
argument_list|,
name|Factory
argument_list|>
name|postingFormatFactories
parameter_list|,
annotation|@
name|Assisted
name|String
name|name
parameter_list|,
annotation|@
name|Assisted
name|Settings
name|postingsFormatSettings
parameter_list|)
block|{
name|super
argument_list|(
name|name
argument_list|)
expr_stmt|;
name|this
operator|.
name|desiredMaxSaturation
operator|=
name|postingsFormatSettings
operator|.
name|getAsFloat
argument_list|(
literal|"desired_max_saturation"
argument_list|,
literal|0.1f
argument_list|)
expr_stmt|;
name|this
operator|.
name|saturationLimit
operator|=
name|postingsFormatSettings
operator|.
name|getAsFloat
argument_list|(
literal|"saturation_limit"
argument_list|,
literal|0.9f
argument_list|)
expr_stmt|;
name|this
operator|.
name|delegate
operator|=
name|Helper
operator|.
name|lookup
argument_list|(
name|indexSettings
argument_list|,
name|postingsFormatSettings
operator|.
name|get
argument_list|(
literal|"delegate"
argument_list|)
argument_list|,
name|postingFormatFactories
argument_list|)
expr_stmt|;
name|this
operator|.
name|postingsFormat
operator|=
operator|new
name|BloomFilteringPostingsFormat
argument_list|(
name|delegate
operator|.
name|get
argument_list|()
argument_list|,
operator|new
name|CustomBloomFilterFactory
argument_list|(
name|desiredMaxSaturation
argument_list|,
name|saturationLimit
argument_list|)
argument_list|)
expr_stmt|;
block|}
DECL|method|desiredMaxSaturation
specifier|public
name|float
name|desiredMaxSaturation
parameter_list|()
block|{
return|return
name|desiredMaxSaturation
return|;
block|}
DECL|method|saturationLimit
specifier|public
name|float
name|saturationLimit
parameter_list|()
block|{
return|return
name|saturationLimit
return|;
block|}
DECL|method|delegate
specifier|public
name|PostingsFormatProvider
name|delegate
parameter_list|()
block|{
return|return
name|delegate
return|;
block|}
annotation|@
name|Override
DECL|method|get
specifier|public
name|PostingsFormat
name|get
parameter_list|()
block|{
return|return
name|postingsFormat
return|;
block|}
DECL|class|CustomBloomFilterFactory
specifier|static
class|class
name|CustomBloomFilterFactory
extends|extends
name|BloomFilterFactory
block|{
DECL|field|desiredMaxSaturation
specifier|private
specifier|final
name|float
name|desiredMaxSaturation
decl_stmt|;
DECL|field|saturationLimit
specifier|private
specifier|final
name|float
name|saturationLimit
decl_stmt|;
DECL|method|CustomBloomFilterFactory
name|CustomBloomFilterFactory
parameter_list|(
name|float
name|desiredMaxSaturation
parameter_list|,
name|float
name|saturationLimit
parameter_list|)
block|{
name|this
operator|.
name|desiredMaxSaturation
operator|=
name|desiredMaxSaturation
expr_stmt|;
name|this
operator|.
name|saturationLimit
operator|=
name|saturationLimit
expr_stmt|;
block|}
annotation|@
name|Override
DECL|method|getSetForField
specifier|public
name|FuzzySet
name|getSetForField
parameter_list|(
name|SegmentWriteState
name|state
parameter_list|,
name|FieldInfo
name|info
parameter_list|)
block|{
comment|//Assume all of the docs have a unique term (e.g. a primary key) and we hope to maintain a set with desiredMaxSaturation% of bits set
return|return
name|FuzzySet
operator|.
name|createSetBasedOnQuality
argument_list|(
name|state
operator|.
name|segmentInfo
operator|.
name|getDocCount
argument_list|()
argument_list|,
name|desiredMaxSaturation
argument_list|)
return|;
block|}
annotation|@
name|Override
DECL|method|isSaturated
specifier|public
name|boolean
name|isSaturated
parameter_list|(
name|FuzzySet
name|bloomFilter
parameter_list|,
name|FieldInfo
name|fieldInfo
parameter_list|)
block|{
comment|// Don't bother saving bitsets if> saturationLimit % of bits are set - we don't want to
comment|// throw any more memory at this problem.
return|return
name|bloomFilter
operator|.
name|getSaturation
argument_list|()
operator|>
name|saturationLimit
return|;
block|}
block|}
block|}
end_class

end_unit

