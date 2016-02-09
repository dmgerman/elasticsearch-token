begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to Elasticsearch under one or more contributor  * license agreements. See the NOTICE file distributed with  * this work for additional information regarding copyright  * ownership. Elasticsearch licenses this file to you under  * the Apache License, Version 2.0 (the "License"); you may  * not use this file except in compliance with the License.  * You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.search.lookup
package|package
name|org
operator|.
name|elasticsearch
operator|.
name|search
operator|.
name|lookup
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
name|index
operator|.
name|LeafReaderContext
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
name|index
operator|.
name|fielddata
operator|.
name|IndexFieldDataService
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
comment|/**  *  */
end_comment

begin_class
DECL|class|DocLookup
specifier|public
class|class
name|DocLookup
block|{
DECL|field|mapperService
specifier|private
specifier|final
name|MapperService
name|mapperService
decl_stmt|;
DECL|field|fieldDataService
specifier|private
specifier|final
name|IndexFieldDataService
name|fieldDataService
decl_stmt|;
annotation|@
name|Nullable
DECL|field|types
specifier|private
specifier|final
name|String
index|[]
name|types
decl_stmt|;
DECL|method|DocLookup
name|DocLookup
parameter_list|(
name|MapperService
name|mapperService
parameter_list|,
name|IndexFieldDataService
name|fieldDataService
parameter_list|,
annotation|@
name|Nullable
name|String
index|[]
name|types
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
name|fieldDataService
operator|=
name|fieldDataService
expr_stmt|;
name|this
operator|.
name|types
operator|=
name|types
expr_stmt|;
block|}
DECL|method|mapperService
specifier|public
name|MapperService
name|mapperService
parameter_list|()
block|{
return|return
name|this
operator|.
name|mapperService
return|;
block|}
DECL|method|fieldDataService
specifier|public
name|IndexFieldDataService
name|fieldDataService
parameter_list|()
block|{
return|return
name|this
operator|.
name|fieldDataService
return|;
block|}
DECL|method|getLeafDocLookup
specifier|public
name|LeafDocLookup
name|getLeafDocLookup
parameter_list|(
name|LeafReaderContext
name|context
parameter_list|)
block|{
return|return
operator|new
name|LeafDocLookup
argument_list|(
name|mapperService
argument_list|,
name|fieldDataService
argument_list|,
name|types
argument_list|,
name|context
argument_list|)
return|;
block|}
DECL|method|getTypes
specifier|public
name|String
index|[]
name|getTypes
parameter_list|()
block|{
return|return
name|types
return|;
block|}
block|}
end_class

end_unit

