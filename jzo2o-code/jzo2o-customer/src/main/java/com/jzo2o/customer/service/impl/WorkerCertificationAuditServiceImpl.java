package com.jzo2o.customer.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.ObjectUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.jzo2o.common.model.CurrentUserInfo;
import com.jzo2o.common.model.PageResult;
import com.jzo2o.customer.enums.CertificationStatusEnum;
import com.jzo2o.customer.mapper.WorkerCertificationAuditMapper;
import com.jzo2o.customer.model.domain.WorkerCertification;
import com.jzo2o.customer.model.domain.WorkerCertificationAudit;
import com.jzo2o.customer.model.dto.request.CertificationAuditReqDTO;
import com.jzo2o.customer.model.dto.request.WorkerCertificationAuditAddReqDTO;
import com.jzo2o.customer.model.dto.request.WorkerCertificationAuditPageQueryReqDTO;
import com.jzo2o.customer.model.dto.response.RejectReasonResDTO;
import com.jzo2o.customer.model.dto.response.WorkerCertificationAuditResDTO;
import com.jzo2o.customer.model.dto.WorkerCertificationUpdateDTO;
import com.jzo2o.customer.service.IServeProviderService;
import com.jzo2o.customer.service.IWorkerCertificationAuditService;
import com.jzo2o.customer.service.IWorkerCertificationService;
import com.jzo2o.mvc.utils.UserContext;
import com.jzo2o.mysql.utils.PageUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.time.LocalDateTime;

@Service
public class WorkerCertificationAuditServiceImpl extends ServiceImpl<WorkerCertificationAuditMapper, WorkerCertificationAudit> implements IWorkerCertificationAuditService {

    @Resource
    private IWorkerCertificationService workerCertificationService;

    @Resource
    private IServeProviderService serveProviderService;

    @Override
    @Transactional
    public void applyCertification(WorkerCertificationAuditAddReqDTO workerCertificationAuditAddReqDTO) {
        WorkerCertificationAudit workerCertificationAudit = BeanUtil.toBean(workerCertificationAuditAddReqDTO, WorkerCertificationAudit.class);
        workerCertificationAudit.setAuditStatus(0);
        baseMapper.insert(workerCertificationAudit);
        Long serveProviderId = workerCertificationAuditAddReqDTO.getServeProviderId();
        WorkerCertification workerCertification = workerCertificationService.getById(serveProviderId);
        if (ObjectUtil.isNotNull(workerCertification)) {
            workerCertification.setCertificationStatus(CertificationStatusEnum.PROGRESSING.getStatus());
            workerCertificationService.updateById(workerCertification);
        } else {
            workerCertification = new WorkerCertification();
            workerCertification.setId(serveProviderId);
            workerCertification.setCertificationStatus(CertificationStatusEnum.PROGRESSING.getStatus());
            workerCertificationService.save(workerCertification);
        }
    }

    @Override
    public RejectReasonResDTO queryCurrentUserLastRejectReason() {
        LambdaQueryWrapper<WorkerCertificationAudit> queryWrapper = Wrappers.<WorkerCertificationAudit>lambdaQuery()
                .eq(WorkerCertificationAudit::getServeProviderId, UserContext.currentUserId())
                .orderByDesc(WorkerCertificationAudit::getCreateTime)
                .last("limit 1");
        WorkerCertificationAudit workerCertificationAudit = baseMapper.selectOne(queryWrapper);
        return workerCertificationAudit != null
                ? new RejectReasonResDTO(workerCertificationAudit.getRejectReason())
                : new RejectReasonResDTO("");
    }

    @Override
    @Transactional
    public void auditCertification(Long id, CertificationAuditReqDTO certificationAuditReqDTO) {
        CurrentUserInfo currentUserInfo = UserContext.currentUser();
        LambdaUpdateWrapper<WorkerCertificationAudit> updateWrapper = Wrappers.<WorkerCertificationAudit>lambdaUpdate()
                .eq(WorkerCertificationAudit::getId, id)
                .set(WorkerCertificationAudit::getAuditStatus, 1)
                .set(WorkerCertificationAudit::getAuditorId, currentUserInfo.getId())
                .set(WorkerCertificationAudit::getAuditorName, currentUserInfo.getName())
                .set(WorkerCertificationAudit::getAuditTime, LocalDateTime.now())
                .set(WorkerCertificationAudit::getCertificationStatus, certificationAuditReqDTO.getCertificationStatus())
                .set(ObjectUtil.isNotEmpty(certificationAuditReqDTO.getRejectReason()), WorkerCertificationAudit::getRejectReason, certificationAuditReqDTO.getRejectReason());
        super.update(updateWrapper);

        WorkerCertificationAudit workerCertificationAudit = baseMapper.selectById(id);
        WorkerCertificationUpdateDTO workerCertificationUpdateDTO = new WorkerCertificationUpdateDTO();
        workerCertificationUpdateDTO.setId(workerCertificationAudit.getServeProviderId());
        workerCertificationUpdateDTO.setCertificationStatus(certificationAuditReqDTO.getCertificationStatus());
        if (ObjectUtil.equal(CertificationStatusEnum.SUCCESS.getStatus(), certificationAuditReqDTO.getCertificationStatus())) {
            serveProviderService.updateNameById(workerCertificationAudit.getServeProviderId(), workerCertificationAudit.getName());
            workerCertificationUpdateDTO.setName(workerCertificationAudit.getName());
            workerCertificationUpdateDTO.setIdCardNo(workerCertificationAudit.getIdCardNo());
            workerCertificationUpdateDTO.setFrontImg(workerCertificationAudit.getFrontImg());
            workerCertificationUpdateDTO.setBackImg(workerCertificationAudit.getBackImg());
            workerCertificationUpdateDTO.setCertificationMaterial(workerCertificationAudit.getCertificationMaterial());
            workerCertificationUpdateDTO.setCertificationTime(workerCertificationAudit.getAuditTime());
        }
        workerCertificationService.updateById(workerCertificationUpdateDTO);
    }

    @Override
    public PageResult<WorkerCertificationAuditResDTO> pageQuery(WorkerCertificationAuditPageQueryReqDTO workerCertificationAuditPageQueryReqDTO) {
        Page<WorkerCertificationAudit> page = PageUtils.parsePageQuery(workerCertificationAuditPageQueryReqDTO, WorkerCertificationAudit.class);
        LambdaQueryWrapper<WorkerCertificationAudit> queryWrapper = Wrappers.<WorkerCertificationAudit>lambdaQuery()
                .like(ObjectUtil.isNotEmpty(workerCertificationAuditPageQueryReqDTO.getName()), WorkerCertificationAudit::getName, workerCertificationAuditPageQueryReqDTO.getName())
                .eq(ObjectUtil.isNotEmpty(workerCertificationAuditPageQueryReqDTO.getIdCardNo()), WorkerCertificationAudit::getIdCardNo, workerCertificationAuditPageQueryReqDTO.getIdCardNo())
                .eq(ObjectUtil.isNotEmpty(workerCertificationAuditPageQueryReqDTO.getAuditStatus()), WorkerCertificationAudit::getAuditStatus, workerCertificationAuditPageQueryReqDTO.getAuditStatus())
                .eq(ObjectUtil.isNotEmpty(workerCertificationAuditPageQueryReqDTO.getCertificationStatus()), WorkerCertificationAudit::getCertificationStatus, workerCertificationAuditPageQueryReqDTO.getCertificationStatus());
        Page<WorkerCertificationAudit> result = baseMapper.selectPage(page, queryWrapper);
        return PageUtils.toPage(result, WorkerCertificationAuditResDTO.class);
    }
}
