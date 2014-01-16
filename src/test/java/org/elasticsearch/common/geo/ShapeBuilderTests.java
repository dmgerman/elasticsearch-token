begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to Elasticsearch under one or more contributor  * license agreements. See the NOTICE file distributed with  * this work for additional information regarding copyright  * ownership. Elasticsearch licenses this file to you under  * the Apache License, Version 2.0 (the "License"); you may  * not use this file except in compliance with the License.  * You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.common.geo
package|package
name|org
operator|.
name|elasticsearch
operator|.
name|common
operator|.
name|geo
package|;
end_package

begin_import
import|import
name|com
operator|.
name|spatial4j
operator|.
name|core
operator|.
name|shape
operator|.
name|Point
import|;
end_import

begin_import
import|import
name|com
operator|.
name|spatial4j
operator|.
name|core
operator|.
name|shape
operator|.
name|Rectangle
import|;
end_import

begin_import
import|import
name|com
operator|.
name|spatial4j
operator|.
name|core
operator|.
name|shape
operator|.
name|Shape
import|;
end_import

begin_import
import|import
name|com
operator|.
name|vividsolutions
operator|.
name|jts
operator|.
name|geom
operator|.
name|Coordinate
import|;
end_import

begin_import
import|import
name|com
operator|.
name|vividsolutions
operator|.
name|jts
operator|.
name|geom
operator|.
name|LineString
import|;
end_import

begin_import
import|import
name|com
operator|.
name|vividsolutions
operator|.
name|jts
operator|.
name|geom
operator|.
name|Polygon
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
name|builders
operator|.
name|ShapeBuilder
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
name|ElasticsearchTestCase
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
name|test
operator|.
name|hamcrest
operator|.
name|ElasticsearchGeoAssertions
operator|.
name|assertMultiLineString
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
name|hamcrest
operator|.
name|ElasticsearchGeoAssertions
operator|.
name|assertMultiPolygon
import|;
end_import

begin_comment
comment|/**  * Tests for {@link ShapeBuilder}  */
end_comment

begin_class
DECL|class|ShapeBuilderTests
specifier|public
class|class
name|ShapeBuilderTests
extends|extends
name|ElasticsearchTestCase
block|{
annotation|@
name|Test
DECL|method|testNewPoint
specifier|public
name|void
name|testNewPoint
parameter_list|()
block|{
name|Point
name|point
init|=
name|ShapeBuilder
operator|.
name|newPoint
argument_list|(
operator|-
literal|100
argument_list|,
literal|45
argument_list|)
operator|.
name|build
argument_list|()
decl_stmt|;
name|assertEquals
argument_list|(
operator|-
literal|100D
argument_list|,
name|point
operator|.
name|getX
argument_list|()
argument_list|,
literal|0.0d
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|45D
argument_list|,
name|point
operator|.
name|getY
argument_list|()
argument_list|,
literal|0.0d
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
DECL|method|testNewRectangle
specifier|public
name|void
name|testNewRectangle
parameter_list|()
block|{
name|Rectangle
name|rectangle
init|=
name|ShapeBuilder
operator|.
name|newEnvelope
argument_list|()
operator|.
name|topLeft
argument_list|(
operator|-
literal|45
argument_list|,
literal|30
argument_list|)
operator|.
name|bottomRight
argument_list|(
literal|45
argument_list|,
operator|-
literal|30
argument_list|)
operator|.
name|build
argument_list|()
decl_stmt|;
name|assertEquals
argument_list|(
operator|-
literal|45D
argument_list|,
name|rectangle
operator|.
name|getMinX
argument_list|()
argument_list|,
literal|0.0d
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
operator|-
literal|30D
argument_list|,
name|rectangle
operator|.
name|getMinY
argument_list|()
argument_list|,
literal|0.0d
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|45D
argument_list|,
name|rectangle
operator|.
name|getMaxX
argument_list|()
argument_list|,
literal|0.0d
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|30D
argument_list|,
name|rectangle
operator|.
name|getMaxY
argument_list|()
argument_list|,
literal|0.0d
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
DECL|method|testNewPolygon
specifier|public
name|void
name|testNewPolygon
parameter_list|()
block|{
name|Polygon
name|polygon
init|=
name|ShapeBuilder
operator|.
name|newPolygon
argument_list|()
operator|.
name|point
argument_list|(
operator|-
literal|45
argument_list|,
literal|30
argument_list|)
operator|.
name|point
argument_list|(
literal|45
argument_list|,
literal|30
argument_list|)
operator|.
name|point
argument_list|(
literal|45
argument_list|,
operator|-
literal|30
argument_list|)
operator|.
name|point
argument_list|(
operator|-
literal|45
argument_list|,
operator|-
literal|30
argument_list|)
operator|.
name|point
argument_list|(
operator|-
literal|45
argument_list|,
literal|30
argument_list|)
operator|.
name|toPolygon
argument_list|()
decl_stmt|;
name|LineString
name|exterior
init|=
name|polygon
operator|.
name|getExteriorRing
argument_list|()
decl_stmt|;
name|assertEquals
argument_list|(
name|exterior
operator|.
name|getCoordinateN
argument_list|(
literal|0
argument_list|)
argument_list|,
operator|new
name|Coordinate
argument_list|(
operator|-
literal|45
argument_list|,
literal|30
argument_list|)
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|exterior
operator|.
name|getCoordinateN
argument_list|(
literal|1
argument_list|)
argument_list|,
operator|new
name|Coordinate
argument_list|(
literal|45
argument_list|,
literal|30
argument_list|)
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|exterior
operator|.
name|getCoordinateN
argument_list|(
literal|2
argument_list|)
argument_list|,
operator|new
name|Coordinate
argument_list|(
literal|45
argument_list|,
operator|-
literal|30
argument_list|)
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|exterior
operator|.
name|getCoordinateN
argument_list|(
literal|3
argument_list|)
argument_list|,
operator|new
name|Coordinate
argument_list|(
operator|-
literal|45
argument_list|,
operator|-
literal|30
argument_list|)
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
DECL|method|testLineStringBuilder
specifier|public
name|void
name|testLineStringBuilder
parameter_list|()
block|{
comment|// Building a simple LineString
name|ShapeBuilder
operator|.
name|newLineString
argument_list|()
operator|.
name|point
argument_list|(
operator|-
literal|130.0
argument_list|,
literal|55.0
argument_list|)
operator|.
name|point
argument_list|(
operator|-
literal|130.0
argument_list|,
operator|-
literal|40.0
argument_list|)
operator|.
name|point
argument_list|(
operator|-
literal|15.0
argument_list|,
operator|-
literal|40.0
argument_list|)
operator|.
name|point
argument_list|(
operator|-
literal|20.0
argument_list|,
literal|50.0
argument_list|)
operator|.
name|point
argument_list|(
operator|-
literal|45.0
argument_list|,
literal|50.0
argument_list|)
operator|.
name|point
argument_list|(
operator|-
literal|45.0
argument_list|,
operator|-
literal|15.0
argument_list|)
operator|.
name|point
argument_list|(
operator|-
literal|110.0
argument_list|,
operator|-
literal|15.0
argument_list|)
operator|.
name|point
argument_list|(
operator|-
literal|110.0
argument_list|,
literal|55.0
argument_list|)
operator|.
name|build
argument_list|()
expr_stmt|;
comment|// Building a linestring that needs to be wrapped
name|ShapeBuilder
operator|.
name|newLineString
argument_list|()
operator|.
name|point
argument_list|(
literal|100.0
argument_list|,
literal|50.0
argument_list|)
operator|.
name|point
argument_list|(
literal|110.0
argument_list|,
operator|-
literal|40.0
argument_list|)
operator|.
name|point
argument_list|(
literal|240.0
argument_list|,
operator|-
literal|40.0
argument_list|)
operator|.
name|point
argument_list|(
literal|230.0
argument_list|,
literal|60.0
argument_list|)
operator|.
name|point
argument_list|(
literal|200.0
argument_list|,
literal|60.0
argument_list|)
operator|.
name|point
argument_list|(
literal|200.0
argument_list|,
operator|-
literal|30.0
argument_list|)
operator|.
name|point
argument_list|(
literal|130.0
argument_list|,
operator|-
literal|30.0
argument_list|)
operator|.
name|point
argument_list|(
literal|130.0
argument_list|,
literal|60.0
argument_list|)
operator|.
name|build
argument_list|()
expr_stmt|;
comment|// Building a lineString on the dateline
name|ShapeBuilder
operator|.
name|newLineString
argument_list|()
operator|.
name|point
argument_list|(
operator|-
literal|180.0
argument_list|,
literal|80.0
argument_list|)
operator|.
name|point
argument_list|(
operator|-
literal|180.0
argument_list|,
literal|40.0
argument_list|)
operator|.
name|point
argument_list|(
operator|-
literal|180.0
argument_list|,
operator|-
literal|40.0
argument_list|)
operator|.
name|point
argument_list|(
operator|-
literal|180.0
argument_list|,
operator|-
literal|80.0
argument_list|)
operator|.
name|build
argument_list|()
expr_stmt|;
comment|// Building a lineString on the dateline
name|ShapeBuilder
operator|.
name|newLineString
argument_list|()
operator|.
name|point
argument_list|(
literal|180.0
argument_list|,
literal|80.0
argument_list|)
operator|.
name|point
argument_list|(
literal|180.0
argument_list|,
literal|40.0
argument_list|)
operator|.
name|point
argument_list|(
literal|180.0
argument_list|,
operator|-
literal|40.0
argument_list|)
operator|.
name|point
argument_list|(
literal|180.0
argument_list|,
operator|-
literal|80.0
argument_list|)
operator|.
name|build
argument_list|()
expr_stmt|;
block|}
annotation|@
name|Test
DECL|method|testMultiLineString
specifier|public
name|void
name|testMultiLineString
parameter_list|()
block|{
name|ShapeBuilder
operator|.
name|newMultiLinestring
argument_list|()
operator|.
name|linestring
argument_list|()
operator|.
name|point
argument_list|(
operator|-
literal|100.0
argument_list|,
literal|50.0
argument_list|)
operator|.
name|point
argument_list|(
literal|50.0
argument_list|,
literal|50.0
argument_list|)
operator|.
name|point
argument_list|(
literal|50.0
argument_list|,
literal|20.0
argument_list|)
operator|.
name|point
argument_list|(
operator|-
literal|100.0
argument_list|,
literal|20.0
argument_list|)
operator|.
name|end
argument_list|()
operator|.
name|linestring
argument_list|()
operator|.
name|point
argument_list|(
operator|-
literal|100.0
argument_list|,
literal|20.0
argument_list|)
operator|.
name|point
argument_list|(
literal|50.0
argument_list|,
literal|20.0
argument_list|)
operator|.
name|point
argument_list|(
literal|50.0
argument_list|,
literal|0.0
argument_list|)
operator|.
name|point
argument_list|(
operator|-
literal|100.0
argument_list|,
literal|0.0
argument_list|)
operator|.
name|end
argument_list|()
operator|.
name|build
argument_list|()
expr_stmt|;
comment|// LineString that needs to be wrappped
name|ShapeBuilder
operator|.
name|newMultiLinestring
argument_list|()
operator|.
name|linestring
argument_list|()
operator|.
name|point
argument_list|(
literal|150.0
argument_list|,
literal|60.0
argument_list|)
operator|.
name|point
argument_list|(
literal|200.0
argument_list|,
literal|60.0
argument_list|)
operator|.
name|point
argument_list|(
literal|200.0
argument_list|,
literal|40.0
argument_list|)
operator|.
name|point
argument_list|(
literal|150.0
argument_list|,
literal|40.0
argument_list|)
operator|.
name|end
argument_list|()
operator|.
name|linestring
argument_list|()
operator|.
name|point
argument_list|(
literal|150.0
argument_list|,
literal|20.0
argument_list|)
operator|.
name|point
argument_list|(
literal|200.0
argument_list|,
literal|20.0
argument_list|)
operator|.
name|point
argument_list|(
literal|200.0
argument_list|,
literal|0.0
argument_list|)
operator|.
name|point
argument_list|(
literal|150.0
argument_list|,
literal|0.0
argument_list|)
operator|.
name|end
argument_list|()
operator|.
name|build
argument_list|()
expr_stmt|;
block|}
annotation|@
name|Test
DECL|method|testPolygonSelfIntersection
specifier|public
name|void
name|testPolygonSelfIntersection
parameter_list|()
block|{
try|try
block|{
name|ShapeBuilder
operator|.
name|newPolygon
argument_list|()
operator|.
name|point
argument_list|(
operator|-
literal|40.0
argument_list|,
literal|50.0
argument_list|)
operator|.
name|point
argument_list|(
literal|40.0
argument_list|,
literal|50.0
argument_list|)
operator|.
name|point
argument_list|(
operator|-
literal|40.0
argument_list|,
operator|-
literal|50.0
argument_list|)
operator|.
name|point
argument_list|(
literal|40.0
argument_list|,
operator|-
literal|50.0
argument_list|)
operator|.
name|close
argument_list|()
operator|.
name|build
argument_list|()
expr_stmt|;
name|fail
argument_list|(
literal|"Polygon self-intersection"
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|Throwable
name|e
parameter_list|)
block|{}
block|}
annotation|@
name|Test
DECL|method|testGeoCircle
specifier|public
name|void
name|testGeoCircle
parameter_list|()
block|{
name|ShapeBuilder
operator|.
name|newCircleBuilder
argument_list|()
operator|.
name|center
argument_list|(
literal|0
argument_list|,
literal|0
argument_list|)
operator|.
name|radius
argument_list|(
literal|"100m"
argument_list|)
operator|.
name|build
argument_list|()
expr_stmt|;
name|ShapeBuilder
operator|.
name|newCircleBuilder
argument_list|()
operator|.
name|center
argument_list|(
operator|+
literal|180
argument_list|,
literal|0
argument_list|)
operator|.
name|radius
argument_list|(
literal|"100m"
argument_list|)
operator|.
name|build
argument_list|()
expr_stmt|;
name|ShapeBuilder
operator|.
name|newCircleBuilder
argument_list|()
operator|.
name|center
argument_list|(
operator|-
literal|180
argument_list|,
literal|0
argument_list|)
operator|.
name|radius
argument_list|(
literal|"100m"
argument_list|)
operator|.
name|build
argument_list|()
expr_stmt|;
name|ShapeBuilder
operator|.
name|newCircleBuilder
argument_list|()
operator|.
name|center
argument_list|(
literal|0
argument_list|,
literal|90
argument_list|)
operator|.
name|radius
argument_list|(
literal|"100m"
argument_list|)
operator|.
name|build
argument_list|()
expr_stmt|;
name|ShapeBuilder
operator|.
name|newCircleBuilder
argument_list|()
operator|.
name|center
argument_list|(
literal|0
argument_list|,
operator|-
literal|90
argument_list|)
operator|.
name|radius
argument_list|(
literal|"100m"
argument_list|)
operator|.
name|build
argument_list|()
expr_stmt|;
block|}
annotation|@
name|Test
DECL|method|testPolygonWrapping
specifier|public
name|void
name|testPolygonWrapping
parameter_list|()
block|{
name|Shape
name|shape
init|=
name|ShapeBuilder
operator|.
name|newPolygon
argument_list|()
operator|.
name|point
argument_list|(
operator|-
literal|150.0
argument_list|,
literal|65.0
argument_list|)
operator|.
name|point
argument_list|(
operator|-
literal|250.0
argument_list|,
literal|65.0
argument_list|)
operator|.
name|point
argument_list|(
operator|-
literal|250.0
argument_list|,
operator|-
literal|65.0
argument_list|)
operator|.
name|point
argument_list|(
operator|-
literal|150.0
argument_list|,
operator|-
literal|65.0
argument_list|)
operator|.
name|close
argument_list|()
operator|.
name|build
argument_list|()
decl_stmt|;
name|assertMultiPolygon
argument_list|(
name|shape
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
DECL|method|testLineStringWrapping
specifier|public
name|void
name|testLineStringWrapping
parameter_list|()
block|{
name|Shape
name|shape
init|=
name|ShapeBuilder
operator|.
name|newLineString
argument_list|()
operator|.
name|point
argument_list|(
operator|-
literal|150.0
argument_list|,
literal|65.0
argument_list|)
operator|.
name|point
argument_list|(
operator|-
literal|250.0
argument_list|,
literal|65.0
argument_list|)
operator|.
name|point
argument_list|(
operator|-
literal|250.0
argument_list|,
operator|-
literal|65.0
argument_list|)
operator|.
name|point
argument_list|(
operator|-
literal|150.0
argument_list|,
operator|-
literal|65.0
argument_list|)
operator|.
name|build
argument_list|()
decl_stmt|;
name|assertMultiLineString
argument_list|(
name|shape
argument_list|)
expr_stmt|;
block|}
block|}
end_class

end_unit

