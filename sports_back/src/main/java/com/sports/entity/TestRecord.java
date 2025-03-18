package com.sports.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;
import javax.persistence.*;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import com.fasterxml.jackson.annotation.JsonProperty;

@Data
@Entity
@Table(name = "test_record")
@NamedEntityGraphs({
    @NamedEntityGraph(
        name = "TestRecord.withSportsItem",
        attributeNodes = {
            @NamedAttributeNode("sportsItem"),
            @NamedAttributeNode("student")
        }
    ),
    @NamedEntityGraph(
        name = "TestRecord.withDetails",
        attributeNodes = {
            @NamedAttributeNode("student"),
            @NamedAttributeNode("sportsItem")
        }
    )
})
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class TestRecord implements Serializable {
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "student_number", length = 50)
    private String studentNumber;

    @Column(name = "student_name", length = 50)
    private String studentName;

    @Column(name = "class_name", length = 50)
    private String className;

    @Column(nullable = false)
    private Double score;

    @Column(length = 20)
    private String status;

    @Column(name = "review_comment")
    private String reviewComment;

    @Column(name = "review_time")
    private LocalDateTime reviewTime;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "update_time")
    private LocalDateTime updateTime;

    @Column(name = "updated_by")
    private Long updatedBy;

    @Column(name = "create_time")
    private LocalDateTime createTime;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sports_item_id")
    private SportsItem sportsItem;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "student_id")
    private User student;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updateTime = LocalDateTime.now();
        if (status == null) {
            status = "PENDING";
        }
    }

    @PreUpdate
    protected void onUpdate() {
        this.updateTime = LocalDateTime.now();
    }

    public Long getSportsItemId() {
        return sportsItem != null ? sportsItem.getId() : null;
    }

    public void setSportsItemId(Long sportsItemId) {
        if (this.sportsItem == null) {
            this.sportsItem = new SportsItem();
        }
        this.sportsItem.setId(sportsItemId);
    }

    @JsonProperty("studentInfo")
    public Map<String, String> getStudentInfo() {
        Map<String, String> info = new HashMap<>();
        info.put("studentNumber", this.studentNumber);
        info.put("realName", this.studentName);
        info.put("className", this.className);
        return info;
    }

    public String getStudentNumber() {
        return this.studentNumber;
    }

    public String getStudentName() {
        return this.studentName;
    }

    public String getClassName() {
        return this.className;
    }

    public void setStudent(User student) {
        this.student = student;
        if (student != null) {
            this.studentNumber = student.getStudentNumber();
            this.studentName = student.getRealName();
            this.className = student.getClassName();
        }
    }

    public void setStudentNumber(String studentNumber) {
        this.studentNumber = studentNumber;
        if (this.student == null && studentNumber != null) {
            // 这里需要注入 UserRepository，或者通过其他方式获取 User 信息
            // 暂时只设置 studentNumber
        }
    }

    public SportsItem getSportsItem() {
        return sportsItem;
    }

    public void setSportsItem(SportsItem sportsItem) {
        this.sportsItem = sportsItem;
    }

    @JsonProperty("sportsItem")
    public Map<String, Object> getSportsItemInfo() {
        Map<String, Object> info = new HashMap<>();
        if (sportsItem != null) {
            info.put("id", sportsItem.getId());
            info.put("name", sportsItem.getName());
        }
        return info;
    }

    @JsonProperty("updatedAt")
    public LocalDateTime getUpdatedAt() {
        return this.updateTime;
    }

    @JsonProperty("updatedAt")
    public void setUpdatedAt(LocalDateTime time) {
        this.updateTime = time;
    }
} 