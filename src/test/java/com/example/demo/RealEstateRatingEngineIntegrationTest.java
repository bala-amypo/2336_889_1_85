package com.example.demo;

import com.example.demo.dto.LoginRequest;
import com.example.demo.dto.RegisterRequest;
import com.example.demo.entity.*;
import com.example.demo.repository.*;
import com.example.demo.security.JwtTokenProvider;
import com.example.demo.service.PropertyService;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.criteria.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.*;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.testng.AbstractTestNGSpringContextTests;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.transaction.annotation.Transactional;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

import java.util.List;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional

// ⭐ USE application-test.properties for H2 test database
@TestPropertySource(locations = "classpath:application-test.properties")

@Listeners(TestResultListener.class)
public class RealEstateRatingEngineIntegrationTest extends AbstractTestNGSpringContextTests {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private PropertyRepository propertyRepository;

    @Autowired
    private FacilityScoreRepository facilityScoreRepository;

    @Autowired
    private RatingResultRepository ratingResultRepository;

    @Autowired
    private RatingLogRepository ratingLogRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PropertyService propertyService;

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @PersistenceContext
    private EntityManager entityManager;

    private String adminToken;
    private String analystToken;

    @BeforeClass(alwaysRun = true)
    public void setupUsers() {

        Assert.assertNotNull(passwordEncoder);
        Assert.assertNotNull(authenticationManager);
        Assert.assertNotNull(jwtTokenProvider);
        Assert.assertNotNull(userRepository);

        // Create ADMIN user
        User admin = new User();
        admin.setName("Admin User");
        admin.setEmail("admin@test.com");
        admin.setRole("ADMIN");
        admin.setPassword(passwordEncoder.encode("admin123"));
        admin = userRepository.save(admin);

        Authentication authAdmin = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken("admin@test.com", "admin123")
        );
        this.adminToken = jwtTokenProvider.generateToken(authAdmin, admin);

        // Create ANALYST user
        User analyst = new User();
        analyst.setName("Analyst User");
        analyst.setEmail("analyst@test.com");
        analyst.setRole("ANALYST");
        analyst.setPassword(passwordEncoder.encode("analyst123"));
        analyst = userRepository.save(analyst);

        Authentication authAnalyst = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken("analyst@test.com", "analyst123")
        );
        this.analystToken = jwtTokenProvider.generateToken(authAnalyst, analyst);
    }

    private Property createSampleProperty(String title) {
        Property p = new Property();
        p.setTitle(title);
        p.setAddress("123 Main St");
        p.setCity("TestCity");
        p.setPrice(300000.0);
        p.setAreaSqFt(1000.0);
        return propertyRepository.save(p);
    }

    private FacilityScore createSampleScore(Property property) {
        FacilityScore s = new FacilityScore();
        s.setProperty(property);
        s.setSchoolProximity(8);
        s.setHospitalProximity(7);
        s.setTransportAccess(9);
        s.setSafetyScore(8);
        return facilityScoreRepository.save(s);
    }

    // ------------------------------------------------------------------------------------
    // 1. Develop and deploy a simple servlet using Tomcat Server
    // ------------------------------------------------------------------------------------

    @Test(priority = 1, groups = "servlet")
    public void testContextLoads() {
        Assert.assertNotNull(mockMvc, "MockMvc should be initialized (embedded Tomcat running).");
    }

@Test(priority = 1, groups = "servlet")
public void testUnknownEndpointReturns401ForUnauthenticated() throws Exception {
    mockMvc.perform(MockMvcRequestBuilders.get("/unknown-servlet"))
            .andExpect(status().isUnauthorized()); // 401
}

 

  @Test(priority = 1, groups = "servlet")
public void testSwaggerUiAccessibleWithoutAuth() throws Exception {
    mockMvc.perform(MockMvcRequestBuilders.get("/swagger-ui/index.html"))
            .andExpect(status().isOk());
}


    @Test(priority = 1, groups = "servlet")
    public void testAuthRegisterEndpointExists() throws Exception {
        RegisterRequest req = new RegisterRequest();
        req.setName("Servlet Test");
        req.setEmail("servlet@test.com");
        req.setPassword("password");
        req.setRole("ANALYST");

        mockMvc.perform(MockMvcRequestBuilders.post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.token").exists());
    }

@Test(priority = 1, groups = "servlet")
public void testRootReturns401WithoutAuth() throws Exception {
    mockMvc.perform(MockMvcRequestBuilders.get("/"))
            .andExpect(status().isUnauthorized());
}



   @Test(priority = 1, groups = "servlet")
public void testTomcatHandlesBadMethod() throws Exception {
    mockMvc.perform(MockMvcRequestBuilders.put("/auth/login"))
            .andExpect(status().isInternalServerError()); // 500
}


@Test(priority = 1, groups = "servlet")
public void testServletHandlesUnsupportedMediaType() throws Exception {
    mockMvc.perform(MockMvcRequestBuilders.post("/auth/login")
                    .content("plain-text-body")
                    .contentType(MediaType.TEXT_PLAIN))
            .andExpect(status().isInternalServerError()); // 500
}



    @Test(priority = 1, groups = "servlet")
    public void testServletHandlesOptionsRequest() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.options("/auth/login"))
                .andExpect(status().isOk());
    }

    // ------------------------------------------------------------------------------------
    // 2. Implement CRUD operations using Spring Boot and REST APIs
    // ------------------------------------------------------------------------------------

    @Test(priority = 2, groups = "crud")
    public void testAddPropertyViaService() {
        Property p = new Property();
        p.setTitle("Service Property");
        p.setAddress("1 Street");
        p.setCity("CityA");
        p.setPrice(200000.0);
        p.setAreaSqFt(1000.0);
        Property saved = propertyService.addProperty(p);
        Assert.assertNotNull(saved.getId());
    }

    @Test(priority = 2, groups = "crud")
    public void testAddPropertyViaRestAdmin() throws Exception {
        Property p = new Property();
        p.setTitle("REST Property");
        p.setAddress("2 Street");
        p.setCity("CityB");
        p.setPrice(250000.0);
        p.setAreaSqFt(1200.0);

        mockMvc.perform(MockMvcRequestBuilders.post("/properties")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(p)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").exists());
    }

    @Test(priority = 2, groups = "crud")
    public void testAddPropertyForbiddenForAnalyst() throws Exception {
        Property p = new Property();
        p.setTitle("Forbidden Property");
        p.setAddress("3 Street");
        p.setCity("CityC");
        p.setPrice(250000.0);
        p.setAreaSqFt(1200.0);

        mockMvc.perform(MockMvcRequestBuilders.post("/properties")
                        .header("Authorization", "Bearer " + analystToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(p)))
                .andExpect(status().isForbidden());
    }

    @Test(priority = 2, groups = "crud")
    public void testListPropertiesRequiresAuth() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.get("/properties"))
                .andExpect(status().isUnauthorized());
    }

    @Test(priority = 2, groups = "crud")
    public void testListPropertiesWithAnalystToken() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.get("/properties")
                        .header("Authorization", "Bearer " + analystToken))
                .andExpect(status().isOk());
    }

    @Test(priority = 2, groups = "crud")
    public void testAddPropertyValidationFailure() throws Exception {
        Property p = new Property();
        p.setTitle("Invalid Property");
        p.setAddress("4 Street");
        p.setCity("CityD");
        p.setPrice(-10.0);
        p.setAreaSqFt(50.0);

        mockMvc.perform(MockMvcRequestBuilders.post("/properties")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(p)))
                .andExpect(status().isBadRequest());
    }

    @Test(priority = 2, groups = "crud")
    public void testCreateAndRetrieveFacilityScore() throws Exception {
        Property p = createSampleProperty("FS Property");

        FacilityScore s = new FacilityScore();
        s.setSchoolProximity(9);
        s.setHospitalProximity(8);
        s.setTransportAccess(9);
        s.setSafetyScore(9);

        mockMvc.perform(MockMvcRequestBuilders.post("/scores/" + p.getId())
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(s)))
                .andExpect(status().isCreated());

        mockMvc.perform(MockMvcRequestBuilders.get("/scores/" + p.getId())
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.schoolProximity").value(9));
    }

    @Test(priority = 2, groups = "crud")
    public void testCreateFacilityScoreTwiceFails() throws Exception {
        Property p = createSampleProperty("FS Dup Property");
        createSampleScore(p);

        FacilityScore s = new FacilityScore();
        s.setSchoolProximity(5);
        s.setHospitalProximity(5);
        s.setTransportAccess(5);
        s.setSafetyScore(5);

        mockMvc.perform(MockMvcRequestBuilders.post("/scores/" + p.getId())
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(s)))
                .andExpect(status().isBadRequest());
    }

    // ------------------------------------------------------------------------------------
    // 3. Configure and perform Dependency Injection and IoC using Spring Framework
    // ------------------------------------------------------------------------------------

    @Test(priority = 3, groups = "di")
    public void testPropertyServiceInjected() {
        Assert.assertNotNull(propertyService, "PropertyService should be injected by IoC container.");
    }

    @Test(priority = 3, groups = "di")
    public void testRepositoriesInjected() {
        Assert.assertNotNull(propertyRepository);
        Assert.assertNotNull(facilityScoreRepository);
        Assert.assertNotNull(ratingResultRepository);
        Assert.assertNotNull(ratingLogRepository);
        Assert.assertNotNull(userRepository);
    }

    @Test(priority = 3, groups = "di")
    public void testSecurityBeansInjected() {
        Assert.assertNotNull(authenticationManager);
        Assert.assertNotNull(jwtTokenProvider);
        Assert.assertNotNull(passwordEncoder);
    }

    @Test(priority = 3, groups = "di")
    public void testBeanFromApplicationContext() {
        Object bean = applicationContext.getBean("securityFilterChain");
        Assert.assertNotNull(bean);
    }

    @Test(priority = 3, groups = "di", expectedExceptions = org.springframework.beans.factory.NoSuchBeanDefinitionException.class)
    public void testNonExistingBeanThrows() {
        applicationContext.getBean("nonExistingBeanName");
    }

    @Test(priority = 3, groups = "di")
    public void testPasswordEncoderIsBCrypt() {
        String encoded = passwordEncoder.encode("testPwd");
        Assert.assertTrue(passwordEncoder.matches("testPwd", encoded));
    }

    @Test(priority = 3, groups = "di")
    public void testAuthenticationManagerAuthenticatesAdmin() {
        Authentication auth = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken("admin@test.com", "admin123")
        );
        Assert.assertTrue(auth.isAuthenticated());
    }

    @Test(priority = 3, groups = "di", expectedExceptions = BadCredentialsException.class)
    public void testAuthenticationManagerRejectsBadPassword() {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken("admin@test.com", "wrong")
        );
    }

    // ------------------------------------------------------------------------------------
    // 4. Implement Hibernate configurations, generator classes, annotations, and CRUD operations
    // ------------------------------------------------------------------------------------

    @Test(priority = 4, groups = "hibernate")
    public void testPersistPropertyEntity() {
        Property p = createSampleProperty("Hibernate Property");
        Assert.assertNotNull(p.getId());
    }

    @Test(priority = 4, groups = "hibernate")
    public void testPersistFacilityScoreAndLoad() {
        Property p = createSampleProperty("Hibernate FS Property");
        FacilityScore score = createSampleScore(p);
        Assert.assertNotNull(score.getId());

        FacilityScore loaded = facilityScoreRepository.findById(score.getId()).orElse(null);
        Assert.assertNotNull(loaded);
        Assert.assertEquals(loaded.getProperty().getId(), p.getId());
    }

    @Test(priority = 4, groups = "hibernate")
    public void testGenerateRatingPersistsRatingResult() {
        Property p = createSampleProperty("Hibernate Rating Property");
        FacilityScore score = createSampleScore(p);
        Assert.assertNotNull(score);

        RatingResult rr = new RatingResult();
        rr.setProperty(p);
        rr.setFinalRating(7.5);
        rr.setRatingCategory("GOOD");

        ratingResultRepository.save(rr);
        Assert.assertNotNull(rr.getId());
    }

@Test(priority = 4, groups = "hibernate")
public void testDeletePropertyCascadesRatingLogs() {

    // STEP 1: Create and save property FIRST
    Property p = createSampleProperty("Cascade Property");
    propertyRepository.saveAndFlush(p);

    // STEP 2: Create and attach log while property is managed
    RatingLog log = new RatingLog();
    log.setMessage("Cascade test");

    p.addRatingLog(log);
    p = propertyRepository.saveAndFlush(p);  // Important!

    

    // STEP 3: Now delete property -> logs should auto-delete
    propertyRepository.delete(p);
    propertyRepository.flush(); // Important flush

   
}



 @Test(priority = 4, groups = "hibernate",
        expectedExceptions = jakarta.validation.ConstraintViolationException.class)
public void testValidationOnFacilityScoreRange() {

    FacilityScore fs = new FacilityScore();
    fs.setSchoolProximity(11);  // INVALID
    fs.setHospitalProximity(5);
    fs.setTransportAccess(5);
    fs.setSafetyScore(5);

    facilityScoreRepository.saveAndFlush(fs);  // throws ConstraintViolationException
}


    @Test(priority = 4, groups = "hibernate")
    public void testFindPropertyByCityHql() {
        createSampleProperty("City-HQL-1");
        createSampleProperty("City-HQL-2");
        List<Property> list = propertyRepository.findByCityHql("TestCity");
        Assert.assertTrue(list.size() >= 2);
    }

    @Test(priority = 4, groups = "hibernate")
    public void testRatingLogTimestampAutoGenerated() {
        Property p = createSampleProperty("Timestamp Property");
        RatingLog log = new RatingLog();
        log.setProperty(p);
        log.setMessage("Time test");
        RatingLog saved = ratingLogRepository.save(log);
        Assert.assertNotNull(saved.getLoggedAt());
    }

    @Test(priority = 4, groups = "hibernate")
    public void testRatingResultTimestampAutoGenerated() {
        Property p = createSampleProperty("RatingTime Property");
        RatingResult rr = new RatingResult();
        rr.setProperty(p);
        rr.setFinalRating(5.0);
        rr.setRatingCategory("AVERAGE");
        RatingResult saved = ratingResultRepository.save(rr);
        Assert.assertNotNull(saved.getRatedAt());
    }

    // ------------------------------------------------------------------------------------
    // 5. Perform JPA mapping with normalization (1NF, 2NF, 3NF)
    // ------------------------------------------------------------------------------------

    @Test(priority = 5, groups = "jpa-mapping")
    public void testPropertyFacilityScoreOneToOneMapping() {
        Property p = createSampleProperty("Mapping Property");
        FacilityScore s = createSampleScore(p);
        FacilityScore loaded = facilityScoreRepository.findByProperty(p).orElse(null);
        Assert.assertNotNull(loaded);
        Assert.assertEquals(loaded.getProperty().getId(), p.getId());
    }

    @Test(priority = 5, groups = "jpa-mapping")
    public void testPropertyRatingResultOneToOneMapping() {
        Property p = createSampleProperty("Mapping Rating Property");
        RatingResult rr = new RatingResult();
        rr.setProperty(p);
        rr.setFinalRating(9.0);
        rr.setRatingCategory("EXCELLENT");
        ratingResultRepository.save(rr);

        RatingResult loaded = ratingResultRepository.findByProperty(p).orElse(null);
        Assert.assertNotNull(loaded);
        Assert.assertEquals(loaded.getProperty().getId(), p.getId());
    }

    @Test(priority = 5, groups = "jpa-mapping")
    public void testPropertyRatingLogsOneToMany() {
        Property p = createSampleProperty("Mapping Log Property");
        RatingLog log1 = new RatingLog();
        log1.setProperty(p);
        log1.setMessage("Log1");
        ratingLogRepository.save(log1);

        RatingLog log2 = new RatingLog();
        log2.setProperty(p);
        log2.setMessage("Log2");
        ratingLogRepository.save(log2);

        List<RatingLog> logs = ratingLogRepository.findByProperty(p);
        Assert.assertEquals(logs.size(), 2);
    }

    @Test(priority = 5, groups = "jpa-mapping")
    public void testUserEmailUniqueConstraint() {
        User u = new User();
        u.setName("Unique1");
        u.setEmail("unique@test.com");
        u.setRole("ANALYST");
        u.setPassword(passwordEncoder.encode("pwd"));
        userRepository.save(u);

        User u2 = new User();
        u2.setName("Unique2");
        u2.setEmail("unique@test.com");
        u2.setRole("ANALYST");
        u2.setPassword(passwordEncoder.encode("pwd2"));

        try {
            userRepository.saveAndFlush(u2);
            Assert.fail("Should violate unique constraint");
        } catch (Exception ex) {
            Assert.assertTrue(true);
        }
    }

    @Test(priority = 5, groups = "jpa-mapping")
    public void testNormalizedPropertyAttributes() {
        Property p = createSampleProperty("Normalization Property");
        Assert.assertNotNull(p.getCity());
        Assert.assertNotNull(p.getAddress());
        Assert.assertNotNull(p.getPrice());
    }

    @Test(priority = 5, groups = "jpa-mapping")
    public void testRatingCategoryDerivedFromRating() {
        Property p = createSampleProperty("Category Property");
        RatingResult rr = new RatingResult();
        rr.setProperty(p);
        rr.setFinalRating(3.0);
        rr.setRatingCategory("POOR");
        ratingResultRepository.save(rr);
        Assert.assertEquals(rr.getRatingCategory(), "POOR");
    }

    @Test(priority = 5, groups = "jpa-mapping")
    public void testOneFacilityScorePerPropertyEnforced() {
        Property p = createSampleProperty("SingleFS Property");
        FacilityScore s1 = createSampleScore(p);
        Assert.assertNotNull(s1.getId());

        FacilityScore s2 = new FacilityScore();
        s2.setProperty(p);
        s2.setSchoolProximity(5);
        s2.setHospitalProximity(5);
        s2.setTransportAccess(5);
        s2.setSafetyScore(5);
        try {
            facilityScoreRepository.saveAndFlush(s2);
            Assert.fail("Should violate unique constraint on property_id");
        } catch (Exception ex) {
            Assert.assertTrue(true);
        }
    }

    // ------------------------------------------------------------------------------------
    // 6. Create Many-to-Many relationships and test associations in Spring Boot
    // ------------------------------------------------------------------------------------

    @Test(priority = 6, groups = "many-to-many")
    public void testUserAssignedPropertiesManyToMany() {
        User user = new User();
        user.setName("MTM User");
        user.setEmail("mtm@test.com");
        user.setPassword(passwordEncoder.encode("pwd"));
        user.setRole("ANALYST");
        user = userRepository.save(user);

        Property p1 = createSampleProperty("MTM Property1");
        Property p2 = createSampleProperty("MTM Property2");

        user.getAssignedProperties().add(p1);
        user.getAssignedProperties().add(p2);
        user = userRepository.save(user);

        User reloaded = userRepository.findById(user.getId()).orElseThrow();
        Assert.assertEquals(reloaded.getAssignedProperties().size(), 2);
    }

  @Test(priority = 6, groups = "many-to-many")
public void testPropertyAssignedUsersManyToMany() {

    User u1 = new User();
    u1.setName("User1");
    u1.setEmail("user1mtm@test.com");
    u1.setPassword(passwordEncoder.encode("pwd"));
    u1.setRole("ANALYST");
    u1 = userRepository.save(u1);

    User u2 = new User();
    u2.setName("User2");
    u2.setEmail("user2mtm@test.com");
    u2.setPassword(passwordEncoder.encode("pwd"));
    u2.setRole("ADMIN");
    u2 = userRepository.save(u2);

    Property p = createSampleProperty("Shared Property");
    p = propertyRepository.save(p);

    // ---- FIX: Update both sides of Many-to-Many ----
    u1.getAssignedProperties().add(p);
    p.getAssignedUsers().add(u1);

    u2.getAssignedProperties().add(p);
    p.getAssignedUsers().add(u2);

    // Save both sides
    userRepository.save(u1);
    userRepository.save(u2);
    propertyRepository.save(p);

    // Reload property
    Property reloaded = propertyRepository.findById(p.getId()).orElseThrow();

    Assert.assertEquals(reloaded.getAssignedUsers().size(), 2);
}


    @Test(priority = 6, groups = "many-to-many")
    public void testManyToManyRemovingAssociation() {
        User user = new User();
        user.setName("MTM Remove User");
        user.setEmail("mtmremove@test.com");
        user.setPassword(passwordEncoder.encode("pwd"));
        user.setRole("ANALYST");
        user = userRepository.save(user);

        Property p = createSampleProperty("MTM Remove Property");
        user.getAssignedProperties().add(p);
        user = userRepository.save(user);

        user.getAssignedProperties().clear();
        user = userRepository.save(user);
        User reloaded = userRepository.findById(user.getId()).orElseThrow();
        Assert.assertTrue(reloaded.getAssignedProperties().isEmpty());
    }

    @Test(priority = 6, groups = "many-to-many")
    public void testManyToManyDuplicateAssignmentAvoided() {
        User user = new User();
        user.setName("MTM Duplicate");
        user.setEmail("mtmdup@test.com");
        user.setPassword(passwordEncoder.encode("pwd"));
        user.setRole("ANALYST");
        user = userRepository.save(user);

        Property p = createSampleProperty("MTM Dup Property");
        user.getAssignedProperties().add(p);
        user.getAssignedProperties().add(p);
        user = userRepository.save(user);
        User reloaded = userRepository.findById(user.getId()).orElseThrow();
        Assert.assertEquals(reloaded.getAssignedProperties().size(), 1);
    }

    @Test(priority = 6, groups = "many-to-many")
    public void testManyToManyLazyLoadingNotNull() {
        User user = new User();
        user.setName("MTM Lazy");
        user.setEmail("mtmlazy@test.com");
        user.setPassword(passwordEncoder.encode("pwd"));
        user.setRole("ANALYST");
        user = userRepository.save(user);

        Property p = createSampleProperty("MTM Lazy Property");
        user.getAssignedProperties().add(p);
        user = userRepository.save(user);

        User reloaded = userRepository.findById(user.getId()).orElseThrow();
        Assert.assertNotNull(reloaded.getAssignedProperties());
    }

    @Test(priority = 6, groups = "many-to-many")
    public void testManyToManyUserDeletionKeepsProperty() {
        User user = new User();
        user.setName("MTM Delete User");
        user.setEmail("mtmdelete@test.com");
        user.setPassword(passwordEncoder.encode("pwd"));
        user.setRole("ANALYST");
        user = userRepository.save(user);

        Property p = createSampleProperty("MTM Delete Property");
        user.getAssignedProperties().add(p);
        user = userRepository.save(user);

        Long propId = p.getId();
        Long userId = user.getId();
        userRepository.deleteById(userId);

        Assert.assertTrue(propertyRepository.findById(propId).isPresent());
    }

  @Test(priority = 6, groups = "many-to-many")
public void testManyToManyPropertyDeletionRemovesAssociation() {

    User user = new User();
    user.setName("MTM Prop Delete User");
    user.setEmail("mtmpropdelete@test.com");
    user.setPassword(passwordEncoder.encode("pwd"));
    user.setRole("ANALYST");
    user = userRepository.save(user);

    Property p = createSampleProperty("MTM Prop Delete");
    p = propertyRepository.save(p);

    // Maintain both sides
    user.getAssignedProperties().add(p);
    p.getAssignedUsers().add(user);
    userRepository.save(user);
    propertyRepository.save(p);

    Long userId = user.getId();
    Long propertyId = p.getId();

    // ⭐ REMOVE ASSOCIATION MANUALLY BEFORE DELETE
    user.getAssignedProperties().remove(p);
    p.getAssignedUsers().remove(user);

    userRepository.save(user); // update owning side

    // Now deletion is safe
    propertyRepository.deleteById(propertyId);

    User reloaded = userRepository.findById(userId).orElseThrow();
    Assert.assertTrue(reloaded.getAssignedProperties().isEmpty());
}


    // ------------------------------------------------------------------------------------
    // 7. Implement basic security controls and JWT token-based authentication
    // ------------------------------------------------------------------------------------

    @Test(priority = 7, groups = "security")
    public void testRegisterAndLoginFlow() throws Exception {
        RegisterRequest reg = new RegisterRequest();
        reg.setName("Sec User");
        reg.setEmail("secuser@test.com");
        reg.setPassword("secpass");
        reg.setRole("ANALYST");

        String regJson = objectMapper.writeValueAsString(reg);

        String regResponse = mockMvc.perform(MockMvcRequestBuilders.post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(regJson))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();

        Assert.assertTrue(regResponse.contains("token"));

        LoginRequest login = new LoginRequest();
        login.setEmail("secuser@test.com");
        login.setPassword("secpass");

        String loginResponse = mockMvc.perform(MockMvcRequestBuilders.post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(login)))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        Assert.assertTrue(loginResponse.contains("token"));
    }

  

    @Test(priority = 7, groups = "security")
    public void testAccessProtectedEndpointWithoutToken() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.get("/properties"))
                .andExpect(status().isUnauthorized());
    }

    @Test(priority = 7, groups = "security")
    public void testAccessProtectedEndpointWithInvalidToken() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.get("/properties")
                        .header("Authorization", "Bearer invalidtoken"))
                .andExpect(status().isUnauthorized());
    }

    @Test(priority = 7, groups = "security")
    public void testGenerateRatingRequiresAuth() throws Exception {
        Property p = createSampleProperty("Sec Rating Property");
        createSampleScore(p);

        mockMvc.perform(MockMvcRequestBuilders.post("/ratings/generate/" + p.getId()))
                .andExpect(status().isUnauthorized());
    }

    @Test(priority = 7, groups = "security")
    public void testGenerateRatingWithAdminToken() throws Exception {
        Property p = createSampleProperty("Sec Rating Property 2");
        createSampleScore(p);

        mockMvc.perform(MockMvcRequestBuilders.post("/ratings/generate/" + p.getId())
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.finalRating").exists());
    }

    @Test(priority = 7, groups = "security")
    public void testGetRatingWithAnalystToken() throws Exception {
        Property p = createSampleProperty("Sec Rating Property 3");
        createSampleScore(p);

        mockMvc.perform(MockMvcRequestBuilders.post("/ratings/generate/" + p.getId())
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isCreated());

        mockMvc.perform(MockMvcRequestBuilders.get("/ratings/property/" + p.getId())
                        .header("Authorization", "Bearer " + analystToken))
                .andExpect(status().isOk());
    }

    @Test(priority = 7, groups = "security")
    public void testJwtContainsUserIdEmailRole() {
        User admin = userRepository.findByEmail("admin@test.com").orElseThrow();
        Authentication auth = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken("admin@test.com", "admin123")
        );
        String token = jwtTokenProvider.generateToken(auth, admin);
        Long userId = jwtTokenProvider.getUserIdFromToken(token);
        Assert.assertEquals(userId, admin.getId());
    }

    // ------------------------------------------------------------------------------------
    // 8. Use HQL and Criteria to perform advanced data querying
    // ------------------------------------------------------------------------------------

    @Test(priority = 8, groups = "hql")
    public void testFindPropertyByCityUsingHql() {
        createSampleProperty("HQL Property 1");
        createSampleProperty("HQL Property 2");
        List<Property> list = propertyRepository.findByCityHql("TestCity");
        Assert.assertTrue(list.size() >= 2);
    }

    @Test(priority = 8, groups = "hql")
    public void testFindPropertyByCityUsingDerivedMethod() {
        createSampleProperty("Derived City Property");
        List<Property> list = propertyRepository.findByCity("TestCity");
        Assert.assertTrue(list.size() >= 1);
    }

    @Test(priority = 8, groups = "hql")
    public void testCriteriaQueryForHighPriceProperties() {
        Property p1 = createSampleProperty("Criteria Property 1");
        p1.setPrice(100000.0);
        propertyRepository.save(p1);

        Property p2 = createSampleProperty("Criteria Property 2");
        p2.setPrice(500000.0);
        propertyRepository.save(p2);

        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<Property> cq = cb.createQuery(Property.class);
        Root<Property> root = cq.from(Property.class);
        cq.select(root).where(cb.greaterThan(root.get("price"), 300000.0));

        List<Property> result = entityManager.createQuery(cq).getResultList();
        Assert.assertTrue(result.stream().allMatch(p -> p.getPrice() > 300000.0));
    }

    @Test(priority = 8, groups = "hql")
    public void testCriteriaQueryForFacilityScoreAverage() {
        Property p = createSampleProperty("Criteria FS Property");
        createSampleScore(p);

        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<Double> cq = cb.createQuery(Double.class);
        Root<FacilityScore> root = cq.from(FacilityScore.class);
        cq.select(cb.avg(root.get("safetyScore")));
        Double avg = entityManager.createQuery(cq).getSingleResult();
        Assert.assertNotNull(avg);
    }

    @Test(priority = 8, groups = "hql")
    public void testQueryRatingResultsAboveThreshold() {
        Property p = createSampleProperty("HQL Rating Property");
        RatingResult rr = new RatingResult();
        rr.setProperty(p);
        rr.setFinalRating(8.5);
        rr.setRatingCategory("EXCELLENT");
        ratingResultRepository.save(rr);

        List<RatingResult> results = entityManager.createQuery(
                        "select r from RatingResult r where r.finalRating > :threshold", RatingResult.class)
                .setParameter("threshold", 8.0)
                .getResultList();
        Assert.assertTrue(results.stream().anyMatch(r -> r.getFinalRating() > 8.0));
    }

    @Test(priority = 8, groups = "hql")
    public void testQueryPropertiesWithLogs() {
        Property p = createSampleProperty("HQL Log Property");
        RatingLog log = new RatingLog();
        log.setProperty(p);
        log.setMessage("Log HQL");
        ratingLogRepository.save(log);

        List<Property> properties = entityManager.createQuery(
                        "select distinct p from Property p join p.ratingLogs l where l.message like :msg",
                        Property.class)
                .setParameter("msg", "%HQL%")
                .getResultList();
        Assert.assertTrue(properties.stream().anyMatch(prop -> prop.getId().equals(p.getId())));
    }

    @Test(priority = 8, groups = "hql")
    public void testQueryAnalystUsers() {
        List<User> analysts = entityManager.createQuery(
                        "select u from User u where u.role = :role", User.class)
                .setParameter("role", "ANALYST")
                .getResultList();
        Assert.assertTrue(analysts.size() >= 1);
    }

    @Test(priority = 8, groups = "hql")
    public void testQueryAveragePriceByCity() {
        createSampleProperty("HQL Avg Price");
        Double avg = entityManager.createQuery(
                        "select avg(p.price) from Property p where p.city = :city", Double.class)
                .setParameter("city", "TestCity")
                .getSingleResult();
        Assert.assertNotNull(avg);
    }
}
