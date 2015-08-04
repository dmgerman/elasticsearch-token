begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to Elasticsearch under one or more contributor  * license agreements. See the NOTICE file distributed with  * this work for additional information regarding copyright  * ownership. Elasticsearch licenses this file to you under  * the Apache License, Version 2.0 (the "License"); you may  * not use this file except in compliance with the License.  * You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.search.sort
package|package
name|org
operator|.
name|elasticsearch
operator|.
name|search
operator|.
name|sort
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
name|common
operator|.
name|xcontent
operator|.
name|XContentBuilder
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
name|xcontent
operator|.
name|XContentHelper
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
name|index
operator|.
name|IndexService
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
name|ESSingleNodeTestCase
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
name|TestSearchContext
import|;
end_import

begin_import
import|import
name|org
operator|.
name|junit
operator|.
name|Test
import|;
end_import

begin_import
import|import static
name|org
operator|.
name|elasticsearch
operator|.
name|common
operator|.
name|xcontent
operator|.
name|XContentFactory
operator|.
name|jsonBuilder
import|;
end_import

begin_class
DECL|class|SortParserTests
specifier|public
class|class
name|SortParserTests
extends|extends
name|ESSingleNodeTestCase
block|{
annotation|@
name|Test
DECL|method|testGeoDistanceSortParserManyPointsNoException
specifier|public
name|void
name|testGeoDistanceSortParserManyPointsNoException
parameter_list|()
throws|throws
name|Exception
block|{
name|XContentBuilder
name|mapping
init|=
name|jsonBuilder
argument_list|()
decl_stmt|;
name|mapping
operator|.
name|startObject
argument_list|()
operator|.
name|startObject
argument_list|(
literal|"type"
argument_list|)
operator|.
name|startObject
argument_list|(
literal|"properties"
argument_list|)
operator|.
name|startObject
argument_list|(
literal|"location"
argument_list|)
operator|.
name|field
argument_list|(
literal|"type"
argument_list|,
literal|"geo_point"
argument_list|)
operator|.
name|endObject
argument_list|()
operator|.
name|endObject
argument_list|()
operator|.
name|endObject
argument_list|()
operator|.
name|endObject
argument_list|()
expr_stmt|;
name|IndexService
name|indexService
init|=
name|createIndex
argument_list|(
literal|"testidx"
argument_list|,
name|Settings
operator|.
name|settingsBuilder
argument_list|()
operator|.
name|build
argument_list|()
argument_list|,
literal|"type"
argument_list|,
name|mapping
argument_list|)
decl_stmt|;
name|TestSearchContext
name|context
init|=
operator|(
name|TestSearchContext
operator|)
name|createSearchContext
argument_list|(
name|indexService
argument_list|)
decl_stmt|;
name|context
operator|.
name|setTypes
argument_list|(
literal|"type"
argument_list|)
expr_stmt|;
name|XContentBuilder
name|sortBuilder
init|=
name|jsonBuilder
argument_list|()
decl_stmt|;
name|sortBuilder
operator|.
name|startObject
argument_list|()
expr_stmt|;
name|sortBuilder
operator|.
name|startArray
argument_list|(
literal|"location"
argument_list|)
expr_stmt|;
name|sortBuilder
operator|.
name|startArray
argument_list|()
operator|.
name|value
argument_list|(
literal|1.2
argument_list|)
operator|.
name|value
argument_list|(
literal|3
argument_list|)
operator|.
name|endArray
argument_list|()
operator|.
name|startArray
argument_list|()
operator|.
name|value
argument_list|(
literal|5
argument_list|)
operator|.
name|value
argument_list|(
literal|6
argument_list|)
operator|.
name|endArray
argument_list|()
expr_stmt|;
name|sortBuilder
operator|.
name|endArray
argument_list|()
expr_stmt|;
name|sortBuilder
operator|.
name|field
argument_list|(
literal|"order"
argument_list|,
literal|"desc"
argument_list|)
expr_stmt|;
name|sortBuilder
operator|.
name|field
argument_list|(
literal|"unit"
argument_list|,
literal|"km"
argument_list|)
expr_stmt|;
name|sortBuilder
operator|.
name|field
argument_list|(
literal|"sort_mode"
argument_list|,
literal|"max"
argument_list|)
expr_stmt|;
name|sortBuilder
operator|.
name|endObject
argument_list|()
expr_stmt|;
name|XContentParser
name|parser
init|=
name|XContentHelper
operator|.
name|createParser
argument_list|(
name|sortBuilder
operator|.
name|bytes
argument_list|()
argument_list|)
decl_stmt|;
name|parser
operator|.
name|nextToken
argument_list|()
expr_stmt|;
name|GeoDistanceSortParser
name|geoParser
init|=
operator|new
name|GeoDistanceSortParser
argument_list|()
decl_stmt|;
name|geoParser
operator|.
name|parse
argument_list|(
name|parser
argument_list|,
name|context
argument_list|)
expr_stmt|;
name|sortBuilder
operator|=
name|jsonBuilder
argument_list|()
expr_stmt|;
name|sortBuilder
operator|.
name|startObject
argument_list|()
expr_stmt|;
name|sortBuilder
operator|.
name|startArray
argument_list|(
literal|"location"
argument_list|)
expr_stmt|;
name|sortBuilder
operator|.
name|value
argument_list|(
operator|new
name|GeoPoint
argument_list|(
literal|1.2
argument_list|,
literal|3
argument_list|)
argument_list|)
operator|.
name|value
argument_list|(
operator|new
name|GeoPoint
argument_list|(
literal|1.2
argument_list|,
literal|3
argument_list|)
argument_list|)
expr_stmt|;
name|sortBuilder
operator|.
name|endArray
argument_list|()
expr_stmt|;
name|sortBuilder
operator|.
name|field
argument_list|(
literal|"order"
argument_list|,
literal|"desc"
argument_list|)
expr_stmt|;
name|sortBuilder
operator|.
name|field
argument_list|(
literal|"unit"
argument_list|,
literal|"km"
argument_list|)
expr_stmt|;
name|sortBuilder
operator|.
name|field
argument_list|(
literal|"sort_mode"
argument_list|,
literal|"max"
argument_list|)
expr_stmt|;
name|sortBuilder
operator|.
name|endObject
argument_list|()
expr_stmt|;
name|parse
argument_list|(
name|context
argument_list|,
name|sortBuilder
argument_list|)
expr_stmt|;
name|sortBuilder
operator|=
name|jsonBuilder
argument_list|()
expr_stmt|;
name|sortBuilder
operator|.
name|startObject
argument_list|()
expr_stmt|;
name|sortBuilder
operator|.
name|startArray
argument_list|(
literal|"location"
argument_list|)
expr_stmt|;
name|sortBuilder
operator|.
name|value
argument_list|(
literal|"1,2"
argument_list|)
operator|.
name|value
argument_list|(
literal|"3,4"
argument_list|)
expr_stmt|;
name|sortBuilder
operator|.
name|endArray
argument_list|()
expr_stmt|;
name|sortBuilder
operator|.
name|field
argument_list|(
literal|"order"
argument_list|,
literal|"desc"
argument_list|)
expr_stmt|;
name|sortBuilder
operator|.
name|field
argument_list|(
literal|"unit"
argument_list|,
literal|"km"
argument_list|)
expr_stmt|;
name|sortBuilder
operator|.
name|field
argument_list|(
literal|"sort_mode"
argument_list|,
literal|"max"
argument_list|)
expr_stmt|;
name|sortBuilder
operator|.
name|endObject
argument_list|()
expr_stmt|;
name|parse
argument_list|(
name|context
argument_list|,
name|sortBuilder
argument_list|)
expr_stmt|;
name|sortBuilder
operator|=
name|jsonBuilder
argument_list|()
expr_stmt|;
name|sortBuilder
operator|.
name|startObject
argument_list|()
expr_stmt|;
name|sortBuilder
operator|.
name|startArray
argument_list|(
literal|"location"
argument_list|)
expr_stmt|;
name|sortBuilder
operator|.
name|value
argument_list|(
literal|"s3y0zh7w1z0g"
argument_list|)
operator|.
name|value
argument_list|(
literal|"s6wjr4et3f8v"
argument_list|)
expr_stmt|;
name|sortBuilder
operator|.
name|endArray
argument_list|()
expr_stmt|;
name|sortBuilder
operator|.
name|field
argument_list|(
literal|"order"
argument_list|,
literal|"desc"
argument_list|)
expr_stmt|;
name|sortBuilder
operator|.
name|field
argument_list|(
literal|"unit"
argument_list|,
literal|"km"
argument_list|)
expr_stmt|;
name|sortBuilder
operator|.
name|field
argument_list|(
literal|"sort_mode"
argument_list|,
literal|"max"
argument_list|)
expr_stmt|;
name|sortBuilder
operator|.
name|endObject
argument_list|()
expr_stmt|;
name|parse
argument_list|(
name|context
argument_list|,
name|sortBuilder
argument_list|)
expr_stmt|;
name|sortBuilder
operator|=
name|jsonBuilder
argument_list|()
expr_stmt|;
name|sortBuilder
operator|.
name|startObject
argument_list|()
expr_stmt|;
name|sortBuilder
operator|.
name|startArray
argument_list|(
literal|"location"
argument_list|)
expr_stmt|;
name|sortBuilder
operator|.
name|value
argument_list|(
literal|1.2
argument_list|)
operator|.
name|value
argument_list|(
literal|3
argument_list|)
expr_stmt|;
name|sortBuilder
operator|.
name|endArray
argument_list|()
expr_stmt|;
name|sortBuilder
operator|.
name|field
argument_list|(
literal|"order"
argument_list|,
literal|"desc"
argument_list|)
expr_stmt|;
name|sortBuilder
operator|.
name|field
argument_list|(
literal|"unit"
argument_list|,
literal|"km"
argument_list|)
expr_stmt|;
name|sortBuilder
operator|.
name|field
argument_list|(
literal|"sort_mode"
argument_list|,
literal|"max"
argument_list|)
expr_stmt|;
name|sortBuilder
operator|.
name|endObject
argument_list|()
expr_stmt|;
name|parse
argument_list|(
name|context
argument_list|,
name|sortBuilder
argument_list|)
expr_stmt|;
name|sortBuilder
operator|=
name|jsonBuilder
argument_list|()
expr_stmt|;
name|sortBuilder
operator|.
name|startObject
argument_list|()
expr_stmt|;
name|sortBuilder
operator|.
name|field
argument_list|(
literal|"location"
argument_list|,
operator|new
name|GeoPoint
argument_list|(
literal|1
argument_list|,
literal|2
argument_list|)
argument_list|)
expr_stmt|;
name|sortBuilder
operator|.
name|field
argument_list|(
literal|"order"
argument_list|,
literal|"desc"
argument_list|)
expr_stmt|;
name|sortBuilder
operator|.
name|field
argument_list|(
literal|"unit"
argument_list|,
literal|"km"
argument_list|)
expr_stmt|;
name|sortBuilder
operator|.
name|field
argument_list|(
literal|"sort_mode"
argument_list|,
literal|"max"
argument_list|)
expr_stmt|;
name|sortBuilder
operator|.
name|endObject
argument_list|()
expr_stmt|;
name|parse
argument_list|(
name|context
argument_list|,
name|sortBuilder
argument_list|)
expr_stmt|;
name|sortBuilder
operator|=
name|jsonBuilder
argument_list|()
expr_stmt|;
name|sortBuilder
operator|.
name|startObject
argument_list|()
expr_stmt|;
name|sortBuilder
operator|.
name|field
argument_list|(
literal|"location"
argument_list|,
literal|"1,2"
argument_list|)
expr_stmt|;
name|sortBuilder
operator|.
name|field
argument_list|(
literal|"order"
argument_list|,
literal|"desc"
argument_list|)
expr_stmt|;
name|sortBuilder
operator|.
name|field
argument_list|(
literal|"unit"
argument_list|,
literal|"km"
argument_list|)
expr_stmt|;
name|sortBuilder
operator|.
name|field
argument_list|(
literal|"sort_mode"
argument_list|,
literal|"max"
argument_list|)
expr_stmt|;
name|sortBuilder
operator|.
name|endObject
argument_list|()
expr_stmt|;
name|parse
argument_list|(
name|context
argument_list|,
name|sortBuilder
argument_list|)
expr_stmt|;
name|sortBuilder
operator|=
name|jsonBuilder
argument_list|()
expr_stmt|;
name|sortBuilder
operator|.
name|startObject
argument_list|()
expr_stmt|;
name|sortBuilder
operator|.
name|field
argument_list|(
literal|"location"
argument_list|,
literal|"s3y0zh7w1z0g"
argument_list|)
expr_stmt|;
name|sortBuilder
operator|.
name|field
argument_list|(
literal|"order"
argument_list|,
literal|"desc"
argument_list|)
expr_stmt|;
name|sortBuilder
operator|.
name|field
argument_list|(
literal|"unit"
argument_list|,
literal|"km"
argument_list|)
expr_stmt|;
name|sortBuilder
operator|.
name|field
argument_list|(
literal|"sort_mode"
argument_list|,
literal|"max"
argument_list|)
expr_stmt|;
name|sortBuilder
operator|.
name|endObject
argument_list|()
expr_stmt|;
name|parse
argument_list|(
name|context
argument_list|,
name|sortBuilder
argument_list|)
expr_stmt|;
name|sortBuilder
operator|=
name|jsonBuilder
argument_list|()
expr_stmt|;
name|sortBuilder
operator|.
name|startObject
argument_list|()
expr_stmt|;
name|sortBuilder
operator|.
name|startArray
argument_list|(
literal|"location"
argument_list|)
expr_stmt|;
name|sortBuilder
operator|.
name|value
argument_list|(
operator|new
name|GeoPoint
argument_list|(
literal|1
argument_list|,
literal|2
argument_list|)
argument_list|)
operator|.
name|value
argument_list|(
literal|"s3y0zh7w1z0g"
argument_list|)
operator|.
name|startArray
argument_list|()
operator|.
name|value
argument_list|(
literal|1
argument_list|)
operator|.
name|value
argument_list|(
literal|2
argument_list|)
operator|.
name|endArray
argument_list|()
operator|.
name|value
argument_list|(
literal|"1,2"
argument_list|)
expr_stmt|;
name|sortBuilder
operator|.
name|endArray
argument_list|()
expr_stmt|;
name|sortBuilder
operator|.
name|field
argument_list|(
literal|"order"
argument_list|,
literal|"desc"
argument_list|)
expr_stmt|;
name|sortBuilder
operator|.
name|field
argument_list|(
literal|"unit"
argument_list|,
literal|"km"
argument_list|)
expr_stmt|;
name|sortBuilder
operator|.
name|field
argument_list|(
literal|"sort_mode"
argument_list|,
literal|"max"
argument_list|)
expr_stmt|;
name|sortBuilder
operator|.
name|endObject
argument_list|()
expr_stmt|;
name|parse
argument_list|(
name|context
argument_list|,
name|sortBuilder
argument_list|)
expr_stmt|;
block|}
DECL|method|parse
specifier|protected
name|void
name|parse
parameter_list|(
name|TestSearchContext
name|context
parameter_list|,
name|XContentBuilder
name|sortBuilder
parameter_list|)
throws|throws
name|Exception
block|{
name|XContentParser
name|parser
init|=
name|XContentHelper
operator|.
name|createParser
argument_list|(
name|sortBuilder
operator|.
name|bytes
argument_list|()
argument_list|)
decl_stmt|;
name|parser
operator|.
name|nextToken
argument_list|()
expr_stmt|;
name|GeoDistanceSortParser
name|geoParser
init|=
operator|new
name|GeoDistanceSortParser
argument_list|()
decl_stmt|;
name|geoParser
operator|.
name|parse
argument_list|(
name|parser
argument_list|,
name|context
argument_list|)
expr_stmt|;
block|}
block|}
end_class

end_unit

