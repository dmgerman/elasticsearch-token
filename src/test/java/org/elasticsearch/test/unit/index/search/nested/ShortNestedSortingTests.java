begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to ElasticSearch and Shay Banon under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership. ElasticSearch licenses this  * file to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *  *      http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.test.unit.index.search.nested
package|package
name|org
operator|.
name|elasticsearch
operator|.
name|test
operator|.
name|unit
operator|.
name|index
operator|.
name|search
operator|.
name|nested
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
name|document
operator|.
name|Field
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
name|document
operator|.
name|IntField
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
name|IndexableField
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
name|FieldDataType
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
name|IndexFieldData
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
name|IndexNumericFieldData
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
name|fieldcomparator
operator|.
name|ShortValuesComparatorSource
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
name|fieldcomparator
operator|.
name|SortMode
import|;
end_import

begin_comment
comment|/**  */
end_comment

begin_class
DECL|class|ShortNestedSortingTests
specifier|public
class|class
name|ShortNestedSortingTests
extends|extends
name|AbstractNumberNestedSortingTests
block|{
annotation|@
name|Override
DECL|method|getFieldDataType
specifier|protected
name|FieldDataType
name|getFieldDataType
parameter_list|()
block|{
return|return
operator|new
name|FieldDataType
argument_list|(
literal|"short"
argument_list|)
return|;
block|}
annotation|@
name|Override
DECL|method|createInnerFieldComparator
specifier|protected
name|IndexFieldData
operator|.
name|XFieldComparatorSource
name|createInnerFieldComparator
parameter_list|(
name|String
name|fieldName
parameter_list|,
name|SortMode
name|sortMode
parameter_list|,
name|Object
name|missingValue
parameter_list|)
block|{
name|IndexNumericFieldData
name|fieldData
init|=
name|getForField
argument_list|(
name|fieldName
argument_list|)
decl_stmt|;
return|return
operator|new
name|ShortValuesComparatorSource
argument_list|(
name|fieldData
argument_list|,
name|missingValue
argument_list|,
name|sortMode
argument_list|)
return|;
block|}
annotation|@
name|Override
DECL|method|createField
specifier|protected
name|IndexableField
name|createField
parameter_list|(
name|String
name|name
parameter_list|,
name|int
name|value
parameter_list|,
name|Field
operator|.
name|Store
name|store
parameter_list|)
block|{
return|return
operator|new
name|IntField
argument_list|(
name|name
argument_list|,
name|value
argument_list|,
name|store
argument_list|)
return|;
block|}
block|}
end_class

end_unit

