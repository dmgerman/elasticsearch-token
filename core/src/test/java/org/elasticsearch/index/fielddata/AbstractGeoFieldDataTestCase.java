begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to Elasticsearch under one or more contributor  * license agreements. See the NOTICE file distributed with  * this work for additional information regarding copyright  * ownership. Elasticsearch licenses this file to you under  * the Apache License, Version 2.0 (the "License"); you may  * not use this file except in compliance with the License.  * You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.index.fielddata
package|package
name|org
operator|.
name|elasticsearch
operator|.
name|index
operator|.
name|fielddata
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
name|Document
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
name|GeoPointField
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
name|StringField
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
name|util
operator|.
name|GeoUtils
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|Version
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|cluster
operator|.
name|metadata
operator|.
name|IndexMetaData
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
name|geo
operator|.
name|GeoPoint
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
name|test
operator|.
name|VersionUtils
import|;
end_import

begin_import
import|import static
name|org
operator|.
name|elasticsearch
operator|.
name|test
operator|.
name|geo
operator|.
name|RandomShapeGenerator
operator|.
name|randomPoint
import|;
end_import

begin_import
import|import static
name|org
operator|.
name|hamcrest
operator|.
name|Matchers
operator|.
name|*
import|;
end_import

begin_comment
comment|/**  *  */
end_comment

begin_class
DECL|class|AbstractGeoFieldDataTestCase
specifier|public
specifier|abstract
class|class
name|AbstractGeoFieldDataTestCase
extends|extends
name|AbstractFieldDataImplTestCase
block|{
DECL|field|version
specifier|protected
name|Version
name|version
init|=
name|VersionUtils
operator|.
name|randomVersionBetween
argument_list|(
name|random
argument_list|()
argument_list|,
name|Version
operator|.
name|V_1_0_0
argument_list|,
name|Version
operator|.
name|CURRENT
argument_list|)
decl_stmt|;
annotation|@
name|Override
DECL|method|getFieldDataType
specifier|protected
specifier|abstract
name|FieldDataType
name|getFieldDataType
parameter_list|()
function_decl|;
DECL|method|getFieldDataSettings
specifier|protected
name|Settings
operator|.
name|Builder
name|getFieldDataSettings
parameter_list|()
block|{
return|return
name|Settings
operator|.
name|builder
argument_list|()
operator|.
name|put
argument_list|(
name|IndexMetaData
operator|.
name|SETTING_VERSION_CREATED
argument_list|,
name|version
argument_list|)
return|;
block|}
DECL|method|randomGeoPointField
specifier|protected
name|Field
name|randomGeoPointField
parameter_list|(
name|String
name|fieldName
parameter_list|,
name|Field
operator|.
name|Store
name|store
parameter_list|)
block|{
name|GeoPoint
name|point
init|=
name|randomPoint
argument_list|(
name|random
argument_list|()
argument_list|)
decl_stmt|;
comment|// norelease move to .before(Version.2_2_0) once GeoPointV2 is fully merged
if|if
condition|(
name|version
operator|.
name|onOrBefore
argument_list|(
name|Version
operator|.
name|CURRENT
argument_list|)
condition|)
block|{
return|return
operator|new
name|StringField
argument_list|(
name|fieldName
argument_list|,
name|point
operator|.
name|lat
argument_list|()
operator|+
literal|","
operator|+
name|point
operator|.
name|lon
argument_list|()
argument_list|,
name|store
argument_list|)
return|;
block|}
return|return
operator|new
name|GeoPointField
argument_list|(
name|fieldName
argument_list|,
name|point
operator|.
name|lon
argument_list|()
argument_list|,
name|point
operator|.
name|lat
argument_list|()
argument_list|,
name|store
argument_list|)
return|;
block|}
annotation|@
name|Override
DECL|method|fillAllMissing
specifier|protected
name|void
name|fillAllMissing
parameter_list|()
throws|throws
name|Exception
block|{
name|Document
name|d
init|=
operator|new
name|Document
argument_list|()
decl_stmt|;
name|d
operator|.
name|add
argument_list|(
operator|new
name|StringField
argument_list|(
literal|"_id"
argument_list|,
literal|"1"
argument_list|,
name|Field
operator|.
name|Store
operator|.
name|NO
argument_list|)
argument_list|)
expr_stmt|;
name|writer
operator|.
name|addDocument
argument_list|(
name|d
argument_list|)
expr_stmt|;
name|d
operator|=
operator|new
name|Document
argument_list|()
expr_stmt|;
name|d
operator|.
name|add
argument_list|(
operator|new
name|StringField
argument_list|(
literal|"_id"
argument_list|,
literal|"2"
argument_list|,
name|Field
operator|.
name|Store
operator|.
name|NO
argument_list|)
argument_list|)
expr_stmt|;
name|writer
operator|.
name|addDocument
argument_list|(
name|d
argument_list|)
expr_stmt|;
name|d
operator|=
operator|new
name|Document
argument_list|()
expr_stmt|;
name|d
operator|.
name|add
argument_list|(
operator|new
name|StringField
argument_list|(
literal|"_id"
argument_list|,
literal|"3"
argument_list|,
name|Field
operator|.
name|Store
operator|.
name|NO
argument_list|)
argument_list|)
expr_stmt|;
name|writer
operator|.
name|addDocument
argument_list|(
name|d
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
DECL|method|testSortMultiValuesFields
specifier|public
name|void
name|testSortMultiValuesFields
parameter_list|()
block|{
name|assumeFalse
argument_list|(
literal|"Only test on non geo_point fields"
argument_list|,
name|getFieldDataType
argument_list|()
operator|.
name|equals
argument_list|(
literal|"geo_point"
argument_list|)
argument_list|)
expr_stmt|;
block|}
DECL|method|assertValues
specifier|protected
name|void
name|assertValues
parameter_list|(
name|MultiGeoPointValues
name|values
parameter_list|,
name|int
name|docId
parameter_list|)
block|{
name|assertValues
argument_list|(
name|values
argument_list|,
name|docId
argument_list|,
literal|false
argument_list|)
expr_stmt|;
block|}
DECL|method|assertMissing
specifier|protected
name|void
name|assertMissing
parameter_list|(
name|MultiGeoPointValues
name|values
parameter_list|,
name|int
name|docId
parameter_list|)
block|{
name|assertValues
argument_list|(
name|values
argument_list|,
name|docId
argument_list|,
literal|true
argument_list|)
expr_stmt|;
block|}
DECL|method|assertValues
specifier|private
name|void
name|assertValues
parameter_list|(
name|MultiGeoPointValues
name|values
parameter_list|,
name|int
name|docId
parameter_list|,
name|boolean
name|missing
parameter_list|)
block|{
name|values
operator|.
name|setDocument
argument_list|(
name|docId
argument_list|)
expr_stmt|;
name|int
name|docCount
init|=
name|values
operator|.
name|count
argument_list|()
decl_stmt|;
if|if
condition|(
name|missing
condition|)
block|{
name|assertThat
argument_list|(
name|docCount
argument_list|,
name|equalTo
argument_list|(
literal|0
argument_list|)
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|assertThat
argument_list|(
name|docCount
argument_list|,
name|greaterThan
argument_list|(
literal|0
argument_list|)
argument_list|)
expr_stmt|;
for|for
control|(
name|int
name|i
init|=
literal|0
init|;
name|i
operator|<
name|docCount
condition|;
operator|++
name|i
control|)
block|{
name|assertThat
argument_list|(
name|values
operator|.
name|valueAt
argument_list|(
name|i
argument_list|)
operator|.
name|lat
argument_list|()
argument_list|,
name|allOf
argument_list|(
name|greaterThanOrEqualTo
argument_list|(
name|GeoUtils
operator|.
name|MIN_LAT_INCL
argument_list|)
argument_list|,
name|lessThanOrEqualTo
argument_list|(
name|GeoUtils
operator|.
name|MAX_LAT_INCL
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|values
operator|.
name|valueAt
argument_list|(
name|i
argument_list|)
operator|.
name|lat
argument_list|()
argument_list|,
name|allOf
argument_list|(
name|greaterThanOrEqualTo
argument_list|(
name|GeoUtils
operator|.
name|MIN_LON_INCL
argument_list|)
argument_list|,
name|lessThanOrEqualTo
argument_list|(
name|GeoUtils
operator|.
name|MAX_LON_INCL
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
block|}
block|}
end_class

end_unit

