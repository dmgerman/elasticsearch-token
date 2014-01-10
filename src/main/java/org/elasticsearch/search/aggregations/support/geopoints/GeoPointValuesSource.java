begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to Elasticsearch under one or more contributor  * license agreements. See the NOTICE file distributed with  * this work for additional information regarding copyright  * ownership. Elasticsearch licenses this file to you under  * the Apache License, Version 2.0 (the "License"); you may  * not use this file except in compliance with the License.  * You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.search.aggregations.support.geopoints
package|package
name|org
operator|.
name|elasticsearch
operator|.
name|search
operator|.
name|aggregations
operator|.
name|support
operator|.
name|geopoints
package|;
end_package

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|index
operator|.
name|fielddata
operator|.
name|BytesValues
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
name|fielddata
operator|.
name|GeoPointValues
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|search
operator|.
name|aggregations
operator|.
name|support
operator|.
name|FieldDataSource
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|search
operator|.
name|aggregations
operator|.
name|support
operator|.
name|ValuesSource
import|;
end_import

begin_comment
comment|/**  * A source of geo points.  */
end_comment

begin_class
DECL|class|GeoPointValuesSource
specifier|public
specifier|final
class|class
name|GeoPointValuesSource
implements|implements
name|ValuesSource
block|{
DECL|field|source
specifier|private
specifier|final
name|FieldDataSource
operator|.
name|GeoPoint
name|source
decl_stmt|;
DECL|method|GeoPointValuesSource
specifier|public
name|GeoPointValuesSource
parameter_list|(
name|FieldDataSource
operator|.
name|GeoPoint
name|source
parameter_list|)
block|{
name|this
operator|.
name|source
operator|=
name|source
expr_stmt|;
block|}
annotation|@
name|Override
DECL|method|metaData
specifier|public
name|FieldDataSource
operator|.
name|MetaData
name|metaData
parameter_list|()
block|{
return|return
name|source
operator|.
name|metaData
argument_list|()
return|;
block|}
annotation|@
name|Override
DECL|method|bytesValues
specifier|public
name|BytesValues
name|bytesValues
parameter_list|()
block|{
return|return
name|source
operator|.
name|bytesValues
argument_list|()
return|;
block|}
DECL|method|values
specifier|public
specifier|final
name|GeoPointValues
name|values
parameter_list|()
block|{
return|return
name|source
operator|.
name|geoPointValues
argument_list|()
return|;
block|}
block|}
end_class

end_unit

